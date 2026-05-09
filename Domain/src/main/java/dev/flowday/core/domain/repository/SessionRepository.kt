package dev.flowday.core.domain.repository

import dev.flowday.core.domain.model.FocusSession
import kotlinx.coroutines.flow.Flow

interface SessionRepository {

    // Flow — reactive. UI updates automatically when data changes.
    fun getSessionsStream(): Flow<List<FocusSession>>

    fun getTodaySessionStream(): Flow<List<FocusSession>>

    // suspend — one-shot async. Returns once, doesn't update.
    suspend fun getActiveSession(): FocusSession?

    suspend fun startSession(label: String = ""): Long

    suspend fun endSession(sessionId: Long)

    suspend fun deleteSession(sessionId: Long)
}