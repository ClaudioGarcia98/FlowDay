package dev.flowday.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponseDto(
    val latitude: Double,
    val longitude: Double,
    val current: WeatherCurrentDto
)

@Serializable
data class WeatherCurrentDto(

    @SerialName("temperature_2m")
    val temperature: Double,

    @SerialName("weathercode")
    val weatherCode: Int
)