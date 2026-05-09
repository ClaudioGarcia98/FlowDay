package dev.flowday.core.domain.model

import java.time.Instant
import java.time.LocalDate

data class DailyIntention(
    val id: Long = 0,
    val date: LocalDate,
    val priorities: List<String>,  // max 3 — enforced in the use case, not here
    val eveningReflection: String = "",
    val createdAt: Instant,
) {
    val hasEveningReview: Boolean get() = eveningReflection.isNotBlank()
}
