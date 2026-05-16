package dev.flowday.core.domain.usecases

import dev.flowday.core.domain.repository.SessionRepository
import javax.inject.Inject

class EndSessionUseCase @Inject constructor(
    private val repository: SessionRepository,
) {
    suspend operator fun invoke(sessionId: Long): Result<Unit> {
        return runCatching { repository.endSession(sessionId) }
    }
}