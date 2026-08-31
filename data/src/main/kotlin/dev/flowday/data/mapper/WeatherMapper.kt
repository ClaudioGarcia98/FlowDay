package dev.flowday.data.mapper

import dev.flowday.database.entity.WeatherCacheEntity
import dev.flowday.domain.model.Weather
import dev.flowday.network.mapper.mapWeatherCodeToCondition

fun WeatherCacheEntity.toWeather(): Weather {
    return Weather(
        temperature = temperature,
        condition = mapWeatherCodeToCondition(weatherCode)
    )


}