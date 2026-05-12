package dev.flowday.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dev.flowday.core.database.entity.FocusSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Query("SELECT * FROM focus_sessions")
    fun getSessionsStream(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE startedAtEpochSecond >= :startOfDayEpoch")
    fun getTodaySessionStream(startOfDayEpoch: Long): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE startedAtEpochSecond >= :startEpoch AND startedAtEpochSecond <= :endEpoch")
    fun getSessionsInRangeStream(startEpoch: Long, endEpoch: Long): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE endedAtEpochSecond IS NULL LIMIT 1")
    suspend fun getActiveSession(): FocusSessionEntity?

    @Insert
    suspend fun insertSession(session: FocusSessionEntity): Long

    @Query("UPDATE focus_sessions SET endedAtEpochSecond = :endTime WHERE id = :sessionId")
    suspend fun endSession(sessionId: Long, endTime: Long)

    @Query("DELETE FROM focus_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)
}