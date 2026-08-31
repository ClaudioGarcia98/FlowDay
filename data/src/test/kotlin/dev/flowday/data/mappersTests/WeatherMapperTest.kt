package dev.flowday.data.mappersTests

import dev.flowday.data.mapper.toWeather
import dev.flowday.database.entity.WeatherCacheEntity
import dev.flowday.domain.model.EnumWeatherCondition
import junit.framework.TestCase.assertEquals
import org.junit.Test

class WeatherMapperTest {

    private fun buildWeatherCacheEntity(
        temperature: Double = 15.5,
        weatherCode: Int = 2,
        dateIso: String = "2026-05-18"
    ) = WeatherCacheEntity(
        temperature = temperature,
        weatherCode = weatherCode,
        dateIso = dateIso
    )

    @Test
    fun `maps temperature correctly`() {
        val weatherEntity = buildWeatherCacheEntity()

        val weather = weatherEntity.toWeather()

        assertEquals(15.5, weather.temperature)
    }

    @Test
    fun `maps condition correctly`() {
        val weatherEntity = buildWeatherCacheEntity()

        val weather = weatherEntity.toWeather()

        assertEquals(EnumWeatherCondition.PARTLY_CLOUDY, weather.condition)
    }
}