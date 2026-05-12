package dev.flowday.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.flowday.core.database.dao.HabitDao
import dev.flowday.core.database.dao.IntentionDao
import dev.flowday.core.database.dao.SessionDao
import dev.flowday.core.database.entity.DailyIntentionEntity
import dev.flowday.core.database.entity.FocusSessionEntity
import dev.flowday.core.database.entity.HabitCheckInEntity
import dev.flowday.core.database.entity.HabitEntity
import dev.flowday.core.database.util.Converters

@Database(
    entities = [
        FocusSessionEntity::class,
        HabitEntity::class,
        HabitCheckInEntity::class,
        DailyIntentionEntity::class,
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class FlowDayDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun habitDao(): HabitDao
    abstract fun intentionDao(): IntentionDao
}