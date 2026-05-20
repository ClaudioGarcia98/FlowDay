package dev.flowday.domain.repository

import dev.flowday.domain.model.Weather
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    fun getWeather(latitude: Double, longitude: Double): Flow<Weather?>
}