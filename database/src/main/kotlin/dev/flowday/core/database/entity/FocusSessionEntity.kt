package dev.flowday.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val startedAtEpochSecond: Long,

    val endedAtEpochSecond: Long?,

    var durationSeconds: Long,

    var label: String,
)