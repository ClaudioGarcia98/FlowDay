package dev.flowday.core.data.repository

import dev.flowday.core.database.dao.HabitDao
import dev.flowday.core.database.dao.SessionDao
import dev.flowday.core.domain.model.WeeklyStats
import dev.flowday.core.domain.repository.AnalyticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

class AnalyticsRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao,
    private val habitDao: HabitDao,
) : AnalyticsRepository {
    override fun getWeeklyStatsStream(weeksBack: Int): Flow<List<WeeklyStats>> {
        val today = LocalDate.now()
        val weekStarts = (0 until weeksBack).map { weeksAgo ->
            today.minusWeeks(weeksAgo.toLong()).with(DayOfWeek.MONDAY)
        }
        return combine(
            sessionDao.getSessionsStream(),
            habitDao.getAllCheckInsStream(),
            habitDao.getHabitsStream()
        ) { sessions, checkIns, habits ->

            weekStarts.map { weekStart ->
                val weekEnd = weekStart.plusDays(6)
                val weekStartEpoch = weekStart.atStartOfDay(ZoneOffset.UTC).toEpochSecond()
                val weekEndEpoch = weekEnd.plusDays(1).atStartOfDay(ZoneOffset.UTC).toEpochSecond()

                val weekSessions = sessions.filter {
                    it.startedAtEpochSecond in weekStartEpoch..<weekEndEpoch
                }

                val weekCheckIns = checkIns.filter {
                    LocalDate.parse(it.dateIso) in weekStart..weekEnd
                }

                val bestDayOfWeek = weekSessions.groupBy { session ->
                    Instant.ofEpochSecond(session.startedAtEpochSecond)
                        .atZone(ZoneOffset.UTC)
                        .dayOfWeek
                }.map { (dayOfWeek, daySessions) ->
                    dayOfWeek to daySessions.sumOf { it.durationSeconds }
                }.maxByOrNull { (_, total) -> total }?.first ?: DayOfWeek.MONDAY

                WeeklyStats(
                    weekStart = weekStart,
                    totalFocusSeconds = weekSessions.filter { it.endedAtEpochSecond != null }
                        .sumOf { it.durationSeconds },
                    sessionCount = weekSessions.size,
                    habitCompletionRate = if (habits.isEmpty()) 0f else weekCheckIns.size.toFloat() / (habits.size * 7),
                    bestFocusDay = weekStart.with(bestDayOfWeek)
                )
            }
        }
    }

    override fun getTotalFocusSecondsStream(): Flow<Long> {
        return sessionDao.getSessionsStream()
            .map { list ->
                list.filter { it.endedAtEpochSecond != null }.sumOf { it.durationSeconds }
            }
    }
}