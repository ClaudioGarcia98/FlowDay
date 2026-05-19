package dev.flowday.core.domain.repository

import dev.flowday.core.domain.model.Weather
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    fun getWeather(latitude: Double, longitude: Double): Flow<Weather?>
}