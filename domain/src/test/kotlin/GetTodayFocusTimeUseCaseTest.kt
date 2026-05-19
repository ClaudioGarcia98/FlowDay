import app.cash.turbine.test
import dev.flowday.core.domain.model.FocusSession
import dev.flowday.core.domain.repository.SessionRepository
import dev.flowday.core.domain.usecases.GetTodayFocusTimeUseCase
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.Instant

class GetTodayFocusTimeUseCaseTest {

    private lateinit var repository: SessionRepository
    private lateinit var getTodayFocusTime: GetTodayFocusTimeUseCase

    @Before
    fun setup() {
        repository = mockk()
        getTodayFocusTime = GetTodayFocusTimeUseCase(repository)
    }

    @Test
    fun `returns total focus time for today`() = runTest {
        every { repository.getTodaySessionStream() } returns flowOf(
            value = listOf(
                fakeSession(durationSeconds = 600, isActive = false),
                fakeSession(durationSeconds = 400, isActive = false),
                fakeSession(durationSeconds = 600, isActive = true),
            )
        )

        getTodayFocusTime().test {
            assertEquals(1000L, awaitItem())
            awaitComplete()
        }

    }

    @Test
    fun `emits zero when no sessions today`() = runTest {
        every { repository.getTodaySessionStream() } returns flowOf(emptyList())

        getTodayFocusTime().test {
            assertEquals(0L, awaitItem())
            awaitComplete()
        }

    }

    @Test
    fun `emits zero when all sessions are active`() = runTest {
        every { repository.getTodaySessionStream() } returns flowOf(
            value = listOf(
                fakeSession(durationSeconds = 600, isActive = true),
                fakeSession(durationSeconds = 400, isActive = true),
                fakeSession(durationSeconds = 600, isActive = true),
            )
        )

        getTodayFocusTime().test {
            assertEquals(0L, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `emits sum of all sessions when none are active`() = runTest {
        every { repository.getTodaySessionStream() } returns flowOf(
            value = listOf(
                fakeSession(durationSeconds = 600, isActive = false),
                fakeSession(durationSeconds = 400, isActive = false),
                fakeSession(durationSeconds = 600, isActive = false),
            )
        )

        getTodayFocusTime().test {
            assertEquals(1600L, awaitItem())
            awaitComplete()
        }
    }

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