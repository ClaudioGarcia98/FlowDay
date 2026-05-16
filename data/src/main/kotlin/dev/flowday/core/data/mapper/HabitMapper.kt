package dev.flowday.core.data.mapper

import dev.flowday.core.database.entity.HabitCheckInEntity
import dev.flowday.core.database.entity.HabitEntity
import dev.flowday.core.domain.model.Habit
import dev.flowday.core.domain.model.HabitCheckIn
import java.time.Instant
import java.time.LocalDate

fun HabitEntity.toHabit(): Habit {
    return Habit(
        id = id,
        name = name,
        iconKey = iconKey,
        currentStreak = 0,
        longestStreak = 0
    )
}

fun HabitCheckInEntity.toHabitCheckIn(): HabitCheckIn {
    return HabitCheckIn(
        id = id,
        habitId = habitId,
        date = LocalDate.parse(dateIso),
        completedAt = Instant.ofEpochSecond(completedAtEpochSecond),
    )
}