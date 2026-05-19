package dev.flowday.core.domain.model

import java.time.Instant


data class FocusSession(
    val id: Long = 0,
    val startedAt: Instant,
    val endedAt: Instant?,
    val durationInSeconds: Long,
    val label: String = "",
) {
    val isActive: Boolean get() = endedAt == null
}
