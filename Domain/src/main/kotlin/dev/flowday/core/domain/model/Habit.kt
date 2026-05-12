package dev.flowday.core.domain.model

import java.time.Instant
import java.time.LocalDate

data class Habit(
    val id: Long = 0,
    val name: String,
    val iconKey: String, // e.g. "book", "run", "water" — maps to an icon in core:ui
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
)

data class HabitCheckIn(
    val id: Long = 0,
    val habitId: Long,
    val date: LocalDate,
    val completedAt: Instant
)

