package dev.flowday.data.repository

import dev.flowday.data.mapper.toDailyIntention
import dev.flowday.database.dao.IntentionDao
import dev.flowday.database.entity.DailyIntentionEntity
import dev.flowday.domain.model.DailyIntention
import dev.flowday.domain.repository.IntentionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

class IntentionRepositoryImpl @Inject constructor(
    private val intentionDao: IntentionDao
) : IntentionRepository {

    override fun getIntentionForDate(date: LocalDate): Flow<DailyIntention?> {
        return intentionDao.getIntentionForDate(date = date.toString())
            .map { it?.toDailyIntention() }
    }

    override suspend fun savePriorities(
        priorities: List<String>,
        date: LocalDate
    ) {
        val entity = DailyIntentionEntity(
            dateIso = date.toString(),
            prioritiesJson = Json.encodeToString(priorities),
            eveningReflection = "",
            createdAtEpochSecond = Instant.now().epochSecond
        )
        return intentionDao.upsertIntention(entity)
    }

    override suspend fun saveEveningReflection(
        reflection: String,
        date: LocalDate
    ) {
        val result = intentionDao.getIntentionForDateOnce(date = date.toString())
            ?.copy(eveningReflection = reflection)

        result ?: return

        return intentionDao.upsertIntention(result)
    }
}