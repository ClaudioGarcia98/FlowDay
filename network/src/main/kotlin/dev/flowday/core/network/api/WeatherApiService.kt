package dev.flowday.core.network.api

import dev.flowday.core.network.dto.WeatherResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude")
        latitude: Double,
        @Query("longitude")
        longitude: Double,
        @Query("current")
        current: String
    ): WeatherResponseDto
}