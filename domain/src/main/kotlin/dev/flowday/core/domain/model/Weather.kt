package dev.flowday.core.domain.model

data class Weather(
    val temperature: Double,
    val condition: EnumWeatherCondition
)

enum class EnumWeatherCondition {
    CLEAR_SKY,
    PARTLY_CLOUDY,
    FOG,
    RAIN,
    SNOW,
    SHOWERS,
    THUNDERSTORM,
    UNKNOWN
}
