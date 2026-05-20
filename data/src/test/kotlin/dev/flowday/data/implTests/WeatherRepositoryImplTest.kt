package dev.flowday.data.implTests

import app.cash.turbine.test
import dev.flowday.data.repository.WeatherRepositoryImpl
import dev.flowday.database.dao.WeatherDao
import dev.flowday.database.entity.WeatherCacheEntity
import dev.flowday.domain.model.EnumWeatherCondition
import dev.flowday.network.api.WeatherApiService
import dev.flowday.network.dto.WeatherCurrentDto
import dev.flowday.network.dto.WeatherResponseDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate

class WeatherRepositoryImplTest {
    private val weatherDao = mockk<WeatherDao>()

    private val weatherApiService = mockk<WeatherApiService>()

    private val repository = WeatherRepositoryImpl(weatherDao, weatherApiService)

    private fun buildWeatherCacheEntity(
        temperature: Double = 15.5,
        weatherCode: Int = 0,
        dateIso: String = LocalDate.now().toString()
    ) = WeatherCacheEntity(
        temperature = temperature,
        weatherCode = weatherCode,
        dateIso = dateIso
    )

    private fun buildWeatherResponseDto(
        latitude: Double = 39.387080,
        longitude: Double = -9.143613,
        current: WeatherCurrentDto = WeatherCurrentDto(
            temperature = 15.5,
            weatherCode = 0
        )
    ) = WeatherResponseDto(
        latitude = latitude,
        longitude = longitude,
        current = current
    )

    @Test
    fun `cache exists and is today returns cached weather`() = runTest {
        val weatherCacheEntity = buildWeatherCacheEntity()

        every { weatherDao.getWeatherCache() } returns flowOf(weatherCacheEntity)

        repository.getWeather(latitude = 39.387080, longitude = -9.143613).test {
            val weather = awaitItem()
            assertEquals(EnumWeatherCondition.CLEAR_SKY, weather?.condition)
            assertEquals(15.5, weather?.temperature)
            awaitComplete()
        }
    }

    @Test
    fun `cache miss fetches from network inserts entity returns mapped weather`() = runTest {

        val weatherResponseDto = buildWeatherResponseDto()

        every { weatherDao.getWeatherCache() } returns flowOf(null)

        coEvery { weatherDao.insertWeather(any()) } returns Unit

        coEvery {
            weatherApiService.getWeather(
                latitude = any(),
                longitude = any(),
                current = any()
            )
        } returns weatherResponseDto

        repository.getWeather(latitude = 39.387080, longitude = -9.143613).test {
            val weather = awaitItem()
            assertEquals(EnumWeatherCondition.CLEAR_SKY, weather?.condition)
            assertEquals(15.5, weather?.temperature)
            awaitComplete()
        }

        coVerify {
            weatherDao.insertWeather(match {
                it.dateIso == LocalDate.now().toString() && it.weatherCode == 0
            })
        }
    }

    @Test
    fun `network fails stale cache exists returns stale cache`() = runTest {

        val weatherCacheEntity = buildWeatherCacheEntity(dateIso = "2026-05-18")

        every { weatherDao.getWeatherCache() } returns flowOf(weatherCacheEntity)

        coEvery {
            weatherApiService.getWeather(
                latitude = any(),
                longitude = any(),
                current = any()
            )
        } throws (Exception("Network error"))

        repository.getWeather(latitude = 39.387080, longitude = -9.143613).test {
            val weather = awaitItem()
            assertEquals(EnumWeatherCondition.CLEAR_SKY, weather?.condition)
            assertEquals(15.5, weather?.temperature)
            awaitComplete()
        }
    }

    @Test
    fun `Network fails no cache returns null`() = runTest {

        every { weatherDao.getWeatherCache() } returns flowOf(null)

        coEvery {
            weatherApiService.getWeather(
                latitude = any(),
                longitude = any(),
                current = any()
            )
        } throws (Exception("Network error"))

        repository.getWeather(latitude = 39.387080, longitude = -9.143613).test {
            val weather = awaitItem()
            assertNull(weather)
            awaitComplete()
        }
    }
}