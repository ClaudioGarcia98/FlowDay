package dev.flowday.domain.usecases

import dev.flowday.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetTodayFocusTimeUseCase @Inject constructor(
    private val repository: SessionRepository,
) {
    operator fun invoke(): Flow<Long> {
        return repository.getTodaySessionStream()
            .map { sessions ->
                sessions
                    .filter { !it.isActive }
                    .sumOf { it.durationInSeconds }
            }
    }
}