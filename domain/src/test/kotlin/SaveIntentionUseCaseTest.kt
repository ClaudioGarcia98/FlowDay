import dev.flowday.core.domain.repository.IntentionRepository
import dev.flowday.core.domain.usecases.SaveIntentionUseCase
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SaveIntentionUseCaseTest {

    private lateinit var repository: IntentionRepository
    private lateinit var saveIntention: SaveIntentionUseCase

    @Before
    fun setup() {
        repository = mockk()
        saveIntention = SaveIntentionUseCase(repository)
    }

    @Test
    fun `fails when priorities is bigger then 3`() = runTest {

        val result = saveIntention(listOf("A", "B", "C", "D"))

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `fails when priorities is empty`() = runTest {

        val result = saveIntention(emptyList())

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `fails when any priority is blank`() = runTest {

        val result = saveIntention(listOf("A", "", "C", "D"))

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `succeeds with 1 priority`() = runTest {

        coEvery { repository.savePriorities(any(), any()) } returns Unit

        val result = saveIntention(listOf("A"))

        assertTrue(result.isSuccess)
    }

    @Test
    fun `succeeds with exactly 3 priorities`() = runTest {

        coEvery { repository.savePriorities(listOf("A", "B", "C"), any()) } returns Unit

        val result = saveIntention(listOf("A", "B", "C"))

        assertTrue(result.isSuccess)
    }

    @Test
    fun `trims whitespace before saving`() = runTest {

        coEvery { repository.savePriorities(listOf("A", "B", "C"), any()) } returns Unit

        val result = saveIntention(listOf(" A", "B ", " C "))

        assertTrue(result.isSuccess)
    }
}