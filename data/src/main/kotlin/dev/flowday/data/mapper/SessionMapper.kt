package dev.flowday.data.mapper

import dev.flowday.database.entity.FocusSessionEntity
import dev.flowday.domain.model.FocusSession
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