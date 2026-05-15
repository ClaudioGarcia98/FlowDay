package dev.flowday.core.domain.usecases

import dev.flowday.core.domain.repository.SessionRepository
import javax.inject.Inject

class StartSessionUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke(label: String = ""): Result<Long> {

        val activeSession = repository.getActiveSession()

        if (activeSession != null) {
            return Result.failure(exception = IllegalStateException("A session is already running"))
        }

        return runCatching { repository.startSession(label) }
    }
}