package dev.flowday.domain.repository

import dev.flowday.domain.model.DailyIntention
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface IntentionRepository {

    fun getIntentionForDate(date: LocalDate): Flow<DailyIntention?>

    suspend fun savePriorities(priorities: List<String>, date: LocalDate)

    suspend fun saveEveningReflection(reflection: String, date: LocalDate)
}