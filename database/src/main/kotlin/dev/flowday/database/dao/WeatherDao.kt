package dev.flowday.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import dev.flowday.database.entity.WeatherCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather_cache")
    fun getWeatherCache(): Flow<WeatherCacheEntity?>

    @Upsert
    suspend fun insertWeather(weather: WeatherCacheEntity)
}