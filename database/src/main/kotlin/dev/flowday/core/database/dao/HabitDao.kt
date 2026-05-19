package dev.flowday.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.flowday.core.database.entity.HabitCheckInEntity
import dev.flowday.core.database.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits")
    fun getHabitsStream(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habit_check_ins WHERE dateIso  = :date")
    fun getCheckInsForDate(date: String): Flow<List<HabitCheckInEntity>>

    @Query("SELECT * FROM habit_check_ins WHERE habitId  = :habitId")
    fun getCheckInsForHabit(habitId: Long): Flow<List<HabitCheckInEntity>>

    @Query("SELECT * FROM habit_check_ins")
    fun getAllCheckInsStream(): Flow<List<HabitCheckInEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCheckIn(checkIn: HabitCheckInEntity)

    @Query("DELETE FROM habit_check_ins WHERE habitId = :habitId AND dateIso = :date")
    suspend fun undoCheckIn(habitId: Long, date: String)

    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteHabit(habitId: Long)
}