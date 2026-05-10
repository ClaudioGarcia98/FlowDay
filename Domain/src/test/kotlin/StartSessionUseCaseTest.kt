import app.cash.turbine.test
import dev.flowday.core.domain.model.FocusSession
import dev.flowday.core.domain.repository.SessionRepository
import dev.flowday.core.domain.usecases.GetTodayFocusTimeUseCase
import dev.flowday.core.domain.usecases.StartSessionUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.Instant

class StartSessionUseCaseTest {

    private lateinit var repository: SessionRepository
    private lateinit var startSession: StartSessionUseCase
    private lateinit var getTodayFocusTime: GetTodayFocusTimeUseCase

    @Before
    fun setup() {
        repository = mockk()
        startSession = StartSessionUseCase(repository)
        getTodayFocusTime = GetTodayFocusTimeUseCase(repository)
    }

    @Test
    fun `fails when a session is already active`() = runTest {

        coEvery { repository.getActiveSession() } returns fakeSession(isActive = true)

        val result = startSession(label = "Deep Work")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun `does not call repository startSession when one is already active`() = runTest {

        coEvery { repository.getActiveSession() } returns fakeSession(isActive = true)

        startSession(label = "Deep work")

        coVerify(exactly = 0) { repository.startSession(label = any()) }
    }

    @Test
    fun `succeeds and returns session id when no active session`() = runTest {

        coEvery { repository.getActiveSession() } returns null
        coEvery { repository.startSession(label = any()) } returns 42L

        val result = startSession(label = "Deep work")

        assertTrue(result.isSuccess)
        assertEquals(42L, result.getOrNull())
    }

    @Test
    fun `focus time sums only completed sessions`() = runTest {
        every { repository.getTodaySessionStream() } returns flowOf(
            value = listOf(
                fakeSession(durationSeconds = 1500, isActive = false),
                fakeSession(durationSeconds = 900, isActive = false),
                fakeSession(durationSeconds = 600, isActive = true),
            )
        )

        getTodayFocusTime().test {
            assertEquals(2400L, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `focus time emits zero when no sessions`() = runTest {
        every { repository.getTodaySessionStream() } returns flowOf(emptyList())

        getTodayFocusTime().test {
            assertEquals(0L, awaitItem())
            awaitComplete()
        }
    }


    // Helper — builds a fake FocusSession for tests
    // Lives in the test file, not in main — test data never ships to production
    private fun fakeSession(
        id: Long = 1L,
        durationSeconds: Long = 1500L,
        isActive: Boolean = false,
    ) = FocusSession(
        id = id,
        startedAt = Instant.now(),
        endedAt = if (isActive) null else Instant.now(),
        durationInSeconds = durationSeconds,
    )
}