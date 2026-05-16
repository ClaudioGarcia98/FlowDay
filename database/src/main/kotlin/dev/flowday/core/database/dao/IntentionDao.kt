package dev.flowday.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import dev.flowday.core.database.entity.DailyIntentionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IntentionDao {

    @Query("SELECT * FROM daily_intentions WHERE dateIso = :date")
    fun getIntentionForDate(date: String): Flow<DailyIntentionEntity?>

    @Query("SELECT * FROM daily_intentions WHERE dateIso >= :startDateIso AND dateIso <= :endDateIso")
    fun getIntentionsInRangeStream(
        startDateIso: String,
        endDateIso: String
    ): Flow<List<DailyIntentionEntity>>

    @Query("SELECT * FROM daily_intentions WHERE dateIso = :date")
    suspend fun getIntentionForDateOnce(date: String): DailyIntentionEntity?

    @Upsert
    suspend fun upsertIntention(intention: DailyIntentionEntity)

    @Delete
    suspend fun deleteIntention(intention: DailyIntentionEntity)
}