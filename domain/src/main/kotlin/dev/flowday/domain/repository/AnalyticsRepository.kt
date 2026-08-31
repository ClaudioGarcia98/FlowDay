package dev.flowday.domain.repository

import dev.flowday.domain.model.WeeklyStats
import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {

    fun getWeeklyStatsStream(weeksBack: Int = 8): Flow<List<WeeklyStats>>

    fun getTotalFocusSecondsStream(): Flow<Long>
}