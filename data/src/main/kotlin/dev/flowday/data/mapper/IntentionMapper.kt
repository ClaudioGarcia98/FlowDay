package dev.flowday.data.mapper

import dev.flowday.database.entity.DailyIntentionEntity
import dev.flowday.domain.model.DailyIntention
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate

fun DailyIntentionEntity.toDailyIntention(): DailyIntention {
    return DailyIntention(
        date = LocalDate.parse(dateIso),
        priorities = Json.decodeFromString<List<String>>(prioritiesJson),
        eveningReflection = eveningReflection,
        createdAt = Instant.ofEpochSecond(createdAtEpochSecond)
    )
}