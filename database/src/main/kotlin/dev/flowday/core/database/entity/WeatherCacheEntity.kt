package dev.flowday.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    val temperature: Double,
    val weatherCode: Int,
    @PrimaryKey
    val dateIso: String
)
