package dev.flowday.data.mapper

import dev.flowday.database.entity.HabitCheckInEntity
import dev.flowday.database.entity.HabitEntity
import dev.flowday.domain.model.Habit
import dev.flowday.domain.model.HabitCheckIn
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