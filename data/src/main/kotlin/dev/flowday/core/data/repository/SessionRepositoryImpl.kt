package dev.flowday.core.data.repository

import dev.flowday.core.data.mapper.toFocusSession
import dev.flowday.core.database.dao.SessionDao
import dev.flowday.core.database.entity.FocusSessionEntity
import dev.flowday.core.domain.model.FocusSession
import dev.flowday.core.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao
) : SessionRepository {

    override fun getSessionsStream(): Flow<List<FocusSession>> {
        return sessionDao.getSessionsStream().map { list -> list.map { it.toFocusSession() } }
    }

    override fun getTodaySessionStream(): Flow<List<FocusSession>> {
        val startOfDayEpoch = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toEpochSecond()
        return sessionDao.getTodaySessionStream(startOfDayEpoch)
            .map { list -> list.map { it.toFocusSession() } }
    }

    override suspend fun getActiveSession(): FocusSession? {
        return sessionDao.getActiveSession()?.toFocusSession()
    }

    override suspend fun startSession(label: String): Long {
        val focusSessionEntity = FocusSessionEntity(
            startedAtEpochSecond = Instant.now().epochSecond,
            endedAtEpochSecond = null,
            durationSeconds = 0L,
            label = label
        )
        return sessionDao.insertSession(focusSessionEntity)
    }

    override suspend fun endSession(sessionId: Long) {
        return sessionDao.endSession(sessionId = sessionId, endTime = Instant.now().epochSecond)
    }

    override suspend fun deleteSession(sessionId: Long) {
        sessionDao.deleteSession(sessionId)
    }

}