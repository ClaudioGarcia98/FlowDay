package dev.flowday.core.domain.model

import java.time.LocalDate

data class WeeklyStats(
    val weekStart: LocalDate,
    val totalFocusSeconds: Long,
    val sessionCount: Int,
    val habitCompletionRate: Float, // 0f..1f — e.g. 0.75f means 75% of habits completed
    val bestFocusDay: LocalDate?, // null if no sessions that week
){
    // Convenience — features display minutes, not seconds
    val totalFocusMinutes: Int get() = (totalFocusSeconds / 60).toInt()
}
