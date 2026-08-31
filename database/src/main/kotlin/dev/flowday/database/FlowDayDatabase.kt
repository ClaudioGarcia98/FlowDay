package dev.flowday.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.flowday.database.dao.HabitDao
import dev.flowday.database.dao.IntentionDao
import dev.flowday.database.dao.SessionDao
import dev.flowday.database.dao.WeatherDao
import dev.flowday.database.entity.DailyIntentionEntity
import dev.flowday.database.entity.FocusSessionEntity
import dev.flowday.database.entity.HabitCheckInEntity
import dev.flowday.database.entity.HabitEntity
import dev.flowday.database.entity.WeatherCacheEntity
import dev.flowday.database.util.Converters

@Database(
    entities = [
        FocusSessionEntity::class,
        HabitEntity::class,
        HabitCheckInEntity::class,
        DailyIntentionEntity::class,
        WeatherCacheEntity::class,
    ],
    version = 4
)
@TypeConverters(Converters::class)
abstract class FlowDayDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun habitDao(): HabitDao
    abstract fun intentionDao(): IntentionDao
    abstract fun weatherDao(): WeatherDao
}