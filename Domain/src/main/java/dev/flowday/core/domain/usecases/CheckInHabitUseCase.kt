package dev.flowday.core.domain.usecases

import dev.flowday.core.domain.repository.HabitRepository
import java.time.LocalDate
import javax.inject.Inject

class CheckInHabitUseCase @Inject constructor(
    val repository: HabitRepository

) {
    suspend operator fun invoke(
        habitId: Long,
        date: LocalDate = LocalDate.now()
    ): Result<Unit> {
        if (date.isAfter(LocalDate.now()))
            return Result.failure(exception = IllegalArgumentException("Cannot check in for a future date"))

        return runCatching { repository.checkIn(habitId, date) }
    }
}