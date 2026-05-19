package dev.flowday.core.data.mappersTests

import dev.flowday.core.data.mapper.toDailyIntention
import dev.flowday.core.database.entity.DailyIntentionEntity
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class IntentionMapperTest {

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
    fun `maps date correctly`() {
        val intentionEntity = buildDailyIntentionEntity()

        val intention = intentionEntity.toDailyIntention()

        assertEquals(LocalDate.parse("2026-05-18"), intention.date)
    }

    @Test
    fun `maps priorities correctly`() {
        val intentionEntity = buildDailyIntentionEntity()

        val intention = intentionEntity.toDailyIntention()

        assertEquals(listOf("Work out", "Read", "Code"), intention.priorities)
    }

    @Test
    fun `maps eveningReflection correctly`() {
        val intentionEntity = buildDailyIntentionEntity()

        val intention = intentionEntity.toDailyIntention()

        assertEquals("Code 4h", intention.eveningReflection)
    }

    @Test
    fun `maps createdAt correctly`() {
        val intentionEntity = buildDailyIntentionEntity()

        val intention = intentionEntity.toDailyIntention()

        assertEquals(Instant.ofEpochSecond(1500L), intention.createdAt)
    }

    @Test
    fun `check if hasEveningReview returns true`() {
        val intentionEntity = buildDailyIntentionEntity()

        val intention = intentionEntity.toDailyIntention()

        assertEquals(true, intention.hasEveningReview)
    }

    @Test
    fun `check if hasEveningReview returns false`() {
        val intentionEntity = buildDailyIntentionEntity(eveningReflection = "")

        val intention = intentionEntity.toDailyIntention()

        assertEquals(false, intention.hasEveningReview)
    }
}