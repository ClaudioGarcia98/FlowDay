package dev.flowday.core.data.mapper

import dev.flowday.core.database.entity.FocusSessionEntity
import dev.flowday.core.domain.model.FocusSession
import java.time.Instant


fun FocusSessionEntity.toFocusSession(): FocusSession {
    return FocusSession(
        id = id,
        startedAt = Instant.ofEpochSecond(startedAtEpochSecond),
        endedAt = endedAtEpochSecond?.let { Instant.ofEpochSecond(it) },
        durationInSeconds = durationSeconds,
        label = label
    )
}