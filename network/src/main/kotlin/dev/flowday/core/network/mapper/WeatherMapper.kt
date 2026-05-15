package dev.flowday.core.network.mapper

import dev.flowday.core.domain.model.EnumWeatherCondition
import dev.flowday.core.domain.model.Weather
import dev.flowday.core.network.dto.WeatherResponseDto

fun WeatherResponseDto.toWeather() =
    Weather(
        temperature = current.temperature,
        condition = when (current.weatherCode) {
            0 -> EnumWeatherCondition.CLEAR_SKY
            in 1..3 -> EnumWeatherCondition.PARTLY_CLOUDY
            in 45..48 -> EnumWeatherCondition.FOG
            in 51..67 -> EnumWeatherCondition.RAIN
            in 71..77 -> EnumWeatherCondition.SNOW
            in 80..82 -> EnumWeatherCondition.SHOWERS
            in 95..99 -> EnumWeatherCondition.THUNDERSTORM
            else -> EnumWeatherCondition.UNKNOWN
        }
    )