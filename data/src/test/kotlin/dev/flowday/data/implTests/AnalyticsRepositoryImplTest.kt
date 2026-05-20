package dev.flowday.data.implTests

import app.cash.turbine.test
import dev.flowday.data.repository.AnalyticsRepositoryImpl
import dev.flowday.database.dao.HabitDao
import dev.flowday.database.dao.SessionDao
import dev.flowday.database.entity.FocusSessionEntity
import dev.flowday.database.entity.HabitCheckInEntity
import dev.flowday.database.entity.HabitEntity
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneOffset

class AnalyticsRepositoryImplTest {
    private val sessionDao = mockk<SessionDao>()
    private val habitDao = mockk<HabitDao>()

    private val repository = AnalyticsRepositoryImpl(sessionDao = sessionDao, habitDao = habitDao)

    private fun buildSessionEntity(
        id: Long = 1L,
        startedAtEpochSecond: Long = 1000L,
        endedAtEpochSecond: Long? = 1500,
        durationSeconds: Long = 500L,
        label: String = "test"
    ) = FocusSessionEntity(
        id = id,
        startedAtEpochSecond = startedAtEpochSecond,
        endedAtEpochSecond = endedAtEpochSecond,
        durationSeconds = durationSeconds,
        label = label
    )

    private fun buildHabitEntity(
        id: Long = 1L,
        name: String = "test",
        iconKey: String = "icon Key",
        createdAtEpochSecond: Long = 500L,
    ) = HabitEntity(
        id = id,
        name = name,
        iconKey = iconKey,
        createdAtEpochSecond = createdAtEpochSecond,
    )

    private fun buildHabitCheckInEntity(
        id: Long = 1L,
        habitId: Long = 1L,
        dateIso: String = LocalDate.now().toString(),
        completedAtEpochSecond: Long = 1500L,
    ) = HabitCheckInEntity(
        id = id,
        habitId = habitId,
        dateIso = dateIso,
        completedAtEpochSecond = completedAtEpochSecond
    )

    @Test
    fun `returns correct sum of completed sessions`() = runTest {

        val sessionEntity = buildSessionEntity()

        every { sessionDao.getSessionsStream() } returns flowOf(listOf(sessionEntity))

        repository.getTotalFocusSecondsStream().test {
            val numberOfTotalFocusSession = awaitItem()
            assertEquals(500L, numberOfTotalFocusSession)
            awaitComplete()
        }
    }

    @Test
    fun `returns 0 no active sessions ended`() = runTest {

        val sessionEntity = buildSessionEntity(endedAtEpochSecond = null)

        every { sessionDao.getSessionsStream() } returns flowOf(listOf(sessionEntity))

        repository.getTotalFocusSecondsStream().test {
            val numberOfTotalFocusSession = awaitItem()
            assertEquals(0L, numberOfTotalFocusSession)
            awaitComplete()
        }
    }

    @Test
    fun `returns correct WeeklyStats for a week with sessions and check-ins`() = runTest {

        val sessionEntity = buildSessionEntity(
            startedAtEpochSecond = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toEpochSecond()
        )

        val habitEntity = buildHabitEntity()

        val checkInEntity = buildHabitCheckInEntity()

        every { sessionDao.getSessionsStream() } returns flowOf(listOf(sessionEntity))
        every { habitDao.getHabitsStream() } returns flowOf(listOf(habitEntity))
        every { habitDao.getAllCheckInsStream() } returns flowOf(listOf(checkInEntity))

        repository.getWeeklyStatsStream().test {
            val listOfWeeklyStats = awaitItem()
            assertEquals(8, listOfWeeklyStats.size)
            assertEquals(500L, listOfWeeklyStats.first().totalFocusSeconds)
            assertEquals(1, listOfWeeklyStats.first().sessionCount)
            assertEquals(1f / 7f, listOfWeeklyStats.first().habitCompletionRate)
            awaitComplete()
        }
    }

    @Test
    fun `returns 0 habit completion rate when no habits exist`() = runTest {

        val sessionEntity = buildSessionEntity(
            startedAtEpochSecond = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toEpochSecond()
        )

        every { sessionDao.getSessionsStream() } returns flowOf(listOf(sessionEntity))
        every { habitDao.getHabitsStream() } returns flowOf(emptyList())
        every { habitDao.getAllCheckInsStream() } returns flowOf(emptyList())

        repository.getWeeklyStatsStream().test {
            val listOfWeeklyStats = awaitItem()
            assertEquals(0f, listOfWeeklyStats.first().habitCompletionRate)
            awaitComplete()
        }
    }

    @Test
    fun `sessions outside the week are excluded`() = runTest {

        val sessionEntity = buildSessionEntity(
            startedAtEpochSecond = LocalDate.now().minusWeeks(2).atStartOfDay(ZoneOffset.UTC)
                .toEpochSecond()
        )

        val habitEntity = buildHabitEntity()

        val checkInEntity = buildHabitCheckInEntity()

        every { sessionDao.getSessionsStream() } returns flowOf(listOf(sessionEntity))
        every { habitDao.getHabitsStream() } returns flowOf(listOf(habitEntity))
        every { habitDao.getAllCheckInsStream() } returns flowOf(listOf(checkInEntity))

        repository.getWeeklyStatsStream().test {
            val listOfWeeklyStats = awaitItem()
            assertEquals(0L, listOfWeeklyStats.first().totalFocusSeconds)
            assertEquals(0, listOfWeeklyStats.first().sessionCount)
            awaitComplete()
        }
    }

    @Test
    fun `best focus day is the day with most focus seconds`() = runTest {

        val sessionEntity = buildSessionEntity(
            startedAtEpochSecond = LocalDate.now().with(DayOfWeek.THURSDAY)
                .atStartOfDay(ZoneOffset.UTC).toEpochSecond()
        )

        val sessionEntity2 = buildSessionEntity(
            startedAtEpochSecond = LocalDate.now().with(DayOfWeek.THURSDAY)
                .plusDays(2).atStartOfDay(ZoneOffset.UTC).toEpochSecond(),
            endedAtEpochSecond = 2500L,
            durationSeconds = 1500L
        )

        val habitEntity = buildHabitEntity()

        val checkInEntity = buildHabitCheckInEntity()


        every { sessionDao.getSessionsStream() } returns flowOf(
            listOf(
                sessionEntity,
                sessionEntity2
            )
        )

        every { habitDao.getHabitsStream() } returns flowOf(listOf(habitEntity))
        every { habitDao.getAllCheckInsStream() } returns flowOf(listOf(checkInEntity))

        repository.getWeeklyStatsStream().test {
            val listOfWeeklyStats = awaitItem()
            assertEquals(
                LocalDate.now().with(DayOfWeek.SATURDAY),
                listOfWeeklyStats.first().bestFocusDay
            )
            awaitComplete()
        }
    }

}

