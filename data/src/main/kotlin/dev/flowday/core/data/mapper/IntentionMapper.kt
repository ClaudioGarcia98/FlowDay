package dev.flowday.core.data.mapper

import dev.flowday.core.database.entity.DailyIntentionEntity
import dev.flowday.core.domain.model.DailyIntention
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate

fun DailyIntentionEntity.toDailyIntention(): DailyIntention {
    return DailyIntention(
        id = 0L,
        date = LocalDate.parse(dateIso),
        priorities = Json.decodeFromString<List<String>>(prioritiesJson),
        eveningReflection = eveningReflection,
        createdAt = Instant.ofEpochSecond(createdAtEpochSecond)
    )
}