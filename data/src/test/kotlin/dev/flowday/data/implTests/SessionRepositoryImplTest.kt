package dev.flowday.data.implTests

import app.cash.turbine.test
import dev.flowday.data.repository.SessionRepositoryImpl
import dev.flowday.database.dao.SessionDao
import dev.flowday.database.entity.FocusSessionEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Instant

class SessionRepositoryImplTest {
    private val sessionDao = mockk<SessionDao>()

    private val repository = SessionRepositoryImpl(sessionDao)

    private fun buildSessionEntity(
        id: Long = 1L,
        startedAtEpochSecond: Long = 1000L,
        endedAtEpochSecond: Long? = null,
        durationSeconds: Long = 500L,
        label: String = "test"
    ) = FocusSessionEntity(
        id = id,
        startedAtEpochSecond = startedAtEpochSecond,
        endedAtEpochSecond = endedAtEpochSecond,
        durationSeconds = durationSeconds,
        label = label
    )

    @Test
    fun `getSessionsStream returns mapped sessions`() = runTest {

        val sessionEntity = buildSessionEntity()

        every { sessionDao.getSessionsStream() } returns flowOf(listOf(sessionEntity))

        repository.getSessionsStream().test {
            val listOfFocusSessions = awaitItem()
            assertEquals(1, listOfFocusSessions.size)
            assertEquals(1L, listOfFocusSessions.first().id)
            awaitComplete()
        }
    }

    @Test
    fun `getTodaySessionStream returns today mapped session`() = runTest {

        val sessionEntity = buildSessionEntity()

        every { sessionDao.getTodaySessionStream(any()) } returns flowOf(listOf(sessionEntity))

        repository.getTodaySessionStream().test {
            val listOfFocusSessions = awaitItem()
            assertEquals(1, listOfFocusSessions.size)
            assertEquals(1L, listOfFocusSessions.first().id)
            assertEquals(Instant.ofEpochSecond(1000L), listOfFocusSessions.first().startedAt)
            awaitComplete()
        }
    }

    @Test
    fun `getActiveSession returns null when no active session`() = runTest {

        coEvery { sessionDao.getActiveSession() } returns null

        val focusSession = repository.getActiveSession()

        assertNull(focusSession)
    }

    @Test
    fun `getActiveSession returns mapped session when active session exists`() = runTest {

        val sessionEntity = buildSessionEntity()


        coEvery { sessionDao.getActiveSession() } returns sessionEntity

        val focusSession = repository.getActiveSession()

        assertEquals(1L, focusSession?.id)
    }

    @Test
    fun `startSession calls dao with correct entity`() = runTest {

        coEvery { sessionDao.insertSession(any()) } returns 1L

        repository.startSession("test")

        coVerify { sessionDao.insertSession(match { it.label == "test" && it.endedAtEpochSecond == null }) }
    }

}
