package dev.flowday.core.data.mapper

import dev.flowday.core.database.entity.WeatherCacheEntity
import dev.flowday.core.domain.model.Weather
import dev.flowday.core.network.mapper.mapWeatherCodeToCondition

fun WeatherCacheEntity.toWeather(): Weather {
    return Weather(
        temperature = temperature,
        condition = mapWeatherCodeToCondition(weatherCode)
    )
}