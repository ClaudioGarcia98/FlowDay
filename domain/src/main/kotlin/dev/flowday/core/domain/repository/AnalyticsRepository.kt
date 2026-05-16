package dev.flowday.core.domain.repository

import dev.flowday.core.domain.model.WeeklyStats
import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {

    fun getWeeklyStatsStream(weeksBack: Int = 8): Flow<List<WeeklyStats>>

    fun getTotalFocusSecondsStream(): Flow<Long>
}