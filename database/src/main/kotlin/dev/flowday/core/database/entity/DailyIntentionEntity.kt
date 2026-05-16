package dev.flowday.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_intentions")
data class DailyIntentionEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val dateIso: String,

    val prioritiesJson: String,

    val eveningReflection: String,

    val createdAtEpochSecond: Long
)