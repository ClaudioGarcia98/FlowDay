package dev.flowday.core.data.implTests

import app.cash.turbine.test
import dev.flowday.core.data.repository.IntentionRepositoryImpl
import dev.flowday.core.database.dao.IntentionDao
import dev.flowday.core.database.entity.DailyIntentionEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate

class IntentionRepositoryImplTest {

    private val intentionDao = mockk<IntentionDao>()

    private val repository = IntentionRepositoryImpl(intentionDao)

    private fun buildDailyIntentionEntity(
        dateIso: String = "2026-05-18",
        prioritiesJson: String = """["Work out", "Read", "Code"]""",
        eveningReflection: String = "Code 4h",
        createdAtEpochSecond: Long = 1500L
    ) = DailyIntentionEntity(
        dateIso = dateIso,
        prioritiesJson = prioritiesJson,
        eveningReflection = eveningReflection,
        createdAtEpochSecond = createdAtEpochSecond
    )

    @Test
    fun `getIntentionForDate returns today mapped session`() = runTest {

        val dailyIntentionEntity = buildDailyIntentionEntity()

        every { intentionDao.getIntentionForDate(any()) } returns flowOf(dailyIntentionEntity)

        repository.getIntentionForDate(LocalDate.parse("2026-05-18")).test {
            val dailyIntention = awaitItem()
            assertEquals(LocalDate.parse("2026-05-18"), dailyIntention?.date)
            awaitComplete()
        }
    }

    @Test
    fun `getIntentionForDate returns null`() = runTest {

        every { intentionDao.getIntentionForDate(any()) } returns flowOf(null)

        repository.getIntentionForDate(LocalDate.parse("2026-05-18")).test {
            val dailyIntention = awaitItem()
            assertNull(dailyIntention)
            awaitComplete()
        }
    }

    @Test
    fun `savePriorities calls dao with correct entity`() = runTest {

        coEvery { intentionDao.upsertIntention(any()) } returns Unit

        repository.savePriorities(
            priorities = listOf("Work out", "Read", "Code"),
            date = LocalDate.parse("2026-05-18")
        )

        coVerify {
            intentionDao.upsertIntention(match {
                it.dateIso == "2026-05-18" && it.prioritiesJson == """["Work out","Read","Code"]"""
            })
        }
    }

    @Test
    fun `saveEveningReflection calls dao with updated reflection`() = runTest {

        val intentionEntity = buildDailyIntentionEntity()

        coEvery { intentionDao.upsertIntention(any()) } returns Unit

        coEvery { intentionDao.getIntentionForDateOnce(any()) } returns intentionEntity

        repository.saveEveningReflection(
            reflection = "Code 4h",
            date = LocalDate.parse("2026-05-18")
        )

        coVerify { intentionDao.upsertIntention(match { it.eveningReflection == "Code 4h" }) }

        coVerify { intentionDao.getIntentionForDateOnce(match { it == "2026-05-18" }) }
    }

    @Test
    fun `saveEveningReflection does nothing when no intention exists`() = runTest {

        coEvery { intentionDao.getIntentionForDateOnce(any()) } returns null

        repository.saveEveningReflection(
            reflection = "Code 4h",
            date = LocalDate.parse("2026-05-18")
        )

        coVerify(exactly = 0) { intentionDao.upsertIntention(any()) }
    }
}