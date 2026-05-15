package dev.flowday.core.domain.repository

import dev.flowday.core.domain.model.Habit
import dev.flowday.core.domain.model.HabitCheckIn
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface HabitRepository {

    fun getHabitsStream(): Flow<List<Habit>>

    fun getCheckInsForDate(date: LocalDate): Flow<List<HabitCheckIn>>

    fun getCheckInsForHabit(habitId: Long): Flow<List<HabitCheckIn>>

    suspend fun createHabit(name: String, iconKey: String): Long

    suspend fun checkIn(habitId: Long, date: LocalDate)

    suspend fun undoCheckIn(habitId: Long, date: LocalDate)

    suspend fun deleteHabit(habitId: Long)
}