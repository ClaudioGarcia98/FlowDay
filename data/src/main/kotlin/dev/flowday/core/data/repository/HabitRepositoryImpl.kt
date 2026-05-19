package dev.flowday.core.data.repository

import dev.flowday.core.data.mapper.toHabit
import dev.flowday.core.data.mapper.toHabitCheckIn
import dev.flowday.core.database.dao.HabitDao
import dev.flowday.core.database.entity.HabitCheckInEntity
import dev.flowday.core.database.entity.HabitEntity
import dev.flowday.core.domain.model.Habit
import dev.flowday.core.domain.model.HabitCheckIn
import dev.flowday.core.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

class HabitRepositoryImpl @Inject constructor(
    private val habitDao: HabitDao
) : HabitRepository {
    override fun getHabitsStream(): Flow<List<Habit>> {
        return habitDao.getHabitsStream().map { list -> list.map { it.toHabit() } }
    }

    override fun getCheckInsForDate(date: LocalDate): Flow<List<HabitCheckIn>> {
        return habitDao.getCheckInsForDate(date.toString())
            .map { list -> list.map { it.toHabitCheckIn() } }
    }

    override fun getCheckInsForHabit(habitId: Long): Flow<List<HabitCheckIn>> {
        return habitDao.getCheckInsForHabit(habitId)
            .map { list -> list.map { it.toHabitCheckIn() } }
    }

    override suspend fun createHabit(name: String, iconKey: String): Long {
        val entity = HabitEntity(
            name = name,
            iconKey = iconKey,
            createdAtEpochSecond = Instant.now().epochSecond
        )
        return habitDao.insertHabit(entity)
    }

    override suspend fun checkIn(habitId: Long, date: LocalDate) {
        val entity = HabitCheckInEntity(
            habitId = habitId,
            dateIso = date.toString(),
            completedAtEpochSecond = Instant.now().epochSecond
        )
        return habitDao.insertCheckIn(entity)
    }

    override suspend fun undoCheckIn(habitId: Long, date: LocalDate) {
        return habitDao.undoCheckIn(habitId = habitId, date = date.toString())
    }

    override suspend fun deleteHabit(habitId: Long) {
        return habitDao.deleteHabit(habitId)
    }
}