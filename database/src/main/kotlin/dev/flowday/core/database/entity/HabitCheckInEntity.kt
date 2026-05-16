package dev.flowday.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_check_ins",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    /*Without this,
      every time a habit is deleted Room does a full table scan to find matching check-ins,
      with this is instant*/
    indices = [Index(value = ["habitId"])]
)
data class HabitCheckInEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val habitId: Long,

    val dateIso: String,

    val completedAtEpochSecond: Long
)