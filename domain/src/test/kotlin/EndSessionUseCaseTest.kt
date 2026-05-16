import dev.flowday.core.domain.repository.SessionRepository
import dev.flowday.core.domain.usecases.EndSessionUseCase
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class EndSessionUseCaseTest {

    private lateinit var repository: SessionRepository

    private lateinit var endSession: EndSessionUseCase


    @Before
    fun setup() {
        repository = mockk()
        endSession = EndSessionUseCase(repository)
    }

    @Test
    fun `succeeds when repository completes without error`() = runTest {

        coEvery { repository.endSession(42L) } returns Unit

        val result = endSession(42L)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `returns failure when repository throws`() = runTest {
        // repository.endSession crashes — simulates a database error

        coEvery { repository.endSession(42L) } throws Exception("Database error")

        val result = endSession(42L)

        assertTrue(result.isFailure)
    }
}