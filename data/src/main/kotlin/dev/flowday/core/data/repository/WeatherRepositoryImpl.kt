package dev.flowday.core.data.repository

import dev.flowday.core.data.mapper.toWeather
import dev.flowday.core.database.dao.WeatherDao
import dev.flowday.core.database.entity.WeatherCacheEntity
import dev.flowday.core.domain.model.Weather
import dev.flowday.core.domain.repository.WeatherRepository
import dev.flowday.core.network.api.WeatherApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val weatherDao: WeatherDao,
    private val weatherApiService: WeatherApiService
) : WeatherRepository {
    override fun getWeather(
        latitude: Double,
        longitude: Double
    ): Flow<Weather?> {
        return weatherDao.getWeatherCache().map { cached ->
            val today = LocalDate.now().toString()
            if (cached != null && cached.dateIso == today) {
                return@map cached.toWeather()
            } else {
                try {
                    val response = weatherApiService.getWeather(
                        latitude = latitude,
                        longitude = longitude,
                        current = "temperature_2m,weathercode"
                    )
                    val entity = WeatherCacheEntity(
                        dateIso = today,
                        temperature = response.current.temperature,
                        weatherCode = response.current.weatherCode
                    )
                    weatherDao.insertWeather(entity)
                    entity.toWeather()
                } catch (e: Exception) {
                    cached?.toWeather()
                }
            }
        }
    }
}