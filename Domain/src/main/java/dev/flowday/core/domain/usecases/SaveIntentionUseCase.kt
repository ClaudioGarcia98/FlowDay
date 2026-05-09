package dev.flowday.core.domain.usecases

import dev.flowday.core.domain.repository.IntentionRepository
import java.time.LocalDate
import javax.inject.Inject

class SaveIntentionUseCase @Inject constructor(
    val repository: IntentionRepository,
) {
    suspend operator fun invoke(
        priorities: List<String>,
        date: LocalDate,
    ): Result<Unit> {
        if (priorities.isEmpty() || priorities.size > 3)
            return Result.failure(exception = IllegalArgumentException("Set between 1 and 3 priorities"))

        if (priorities.any { it.isBlank() })
            return Result.failure(exception = IllegalArgumentException("Priorities cannot be empty"))

        return runCatching { repository.savePriorities(priorities.map { it.trim() }, date) }
    }
}