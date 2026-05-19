import dev.flowday.core.domain.repository.HabitRepository
import dev.flowday.core.domain.usecases.CheckInHabitUseCase
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class CheckInHabitUseCaseTest {

    private lateinit var repository: HabitRepository
    private lateinit var checkInHabit: CheckInHabitUseCase

    @Before
    fun setup() {
        repository = mockk()
        checkInHabit = CheckInHabitUseCase(repository)
    }

    @Test
    fun `fails when date is in the future`() = runTest {
        val futureDate = LocalDate.now().plusDays(1)

        val result = checkInHabit(42L, futureDate)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `succeeds when date is in the past`() = runTest {

        coEvery { repository.checkIn(42L, LocalDate.now()) } returns Unit

        val result = checkInHabit(42L)

        assertTrue(result.isSuccess)
    }
}