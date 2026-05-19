package dev.flowday.core.network

import dev.flowday.core.domain.model.EnumWeatherCondition
import dev.flowday.core.network.dto.WeatherCurrentDto
import dev.flowday.core.network.dto.WeatherResponseDto
import dev.flowday.core.network.mapper.toWeather
import junit.framework.TestCase.assertEquals
import org.junit.Test


class WeatherMapperTest {


    @Test
    fun `maps temperature correctly`() {
        val mapper = buildWeatherResponseDto(weatherCode = 0)

        val weather = mapper.toWeather()
        assertEquals(15.2, weather.temperature)
    }

    @Test
    fun `maps condition CLEAR_SKY correctly`() {
        val mapper = buildWeatherResponseDto(weatherCode = 0)
        val weather = mapper.toWeather()
        assertEquals(EnumWeatherCondition.CLEAR_SKY, weather.condition)
    }

    @Test
    fun `maps condition PARTLY_CLOUDY correctly`() {
        val mapper = buildWeatherResponseDto(weatherCode = 2)
        val weather = mapper.toWeather()
        assertEquals(EnumWeatherCondition.PARTLY_CLOUDY, weather.condition)
    }

    @Test
    fun `maps condition FOG correctly`() {
        val mapper = buildWeatherResponseDto(weatherCode = 47)
        val weather = mapper.toWeather()
        assertEquals(EnumWeatherCondition.FOG, weather.condition)
    }

    @Test
    fun `maps condition RAIN correctly`() {
        val mapper = buildWeatherResponseDto(weatherCode = 53)
        val weather = mapper.toWeather()
        assertEquals(EnumWeatherCondition.RAIN, weather.condition)
    }

    @Test
    fun `maps condition SNOW correctly`() {
        val mapper = buildWeatherResponseDto(weatherCode = 75)
        val weather = mapper.toWeather()
        assertEquals(EnumWeatherCondition.SNOW, weather.condition)
    }

    @Test
    fun `maps condition SHOWERS correctly`() {
        val mapper = buildWeatherResponseDto(weatherCode = 81)
        val weather = mapper.toWeather()
        assertEquals(EnumWeatherCondition.SHOWERS, weather.condition)
    }

    @Test
    fun `maps condition THUNDERSTORM correctly`() {
        val mapper = buildWeatherResponseDto(weatherCode = 96)
        val weather = mapper.toWeather()
        assertEquals(EnumWeatherCondition.THUNDERSTORM, weather.condition)
    }

    @Test
    fun `maps condition UNKNOWN correctly`() {
        val mapper = buildWeatherResponseDto(weatherCode = 127)
        val weather = mapper.toWeather()
        assertEquals(EnumWeatherCondition.UNKNOWN, weather.condition)
    }

    private fun buildWeatherResponseDto(
        latitude: Double = 41.1459,
        longitude: Double = -8.6169,
        temperature: Double = 15.2,
        weatherCode: Int
    ) = WeatherResponseDto(
        latitude = latitude,
        longitude = longitude,
        current = WeatherCurrentDto(
            temperature = temperature,
            weatherCode = weatherCode
        )
    )
}