package dev.flowday.data.mappersTests

import dev.flowday.data.mapper.toFocusSession
import dev.flowday.database.entity.FocusSessionEntity
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test
import java.time.Instant

class SessionMapperTest {

    private fun buildSessionEntity(
        id: Long = 0L,
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
    fun `maps id correctly`() {
        val focusSessionEntity = buildSessionEntity(id = 2L)

        val focusSession = focusSessionEntity.toFocusSession()

        assertEquals(2L, focusSession.id)
    }

    @Test
    fun `maps startedAt correctly`() {
        val focusSessionEntity = buildSessionEntity()

        val focusSession = focusSessionEntity.toFocusSession()

        assertEquals(Instant.ofEpochSecond(1000L), focusSession.startedAt)
    }

    @Test
    fun `maps endedAt as null when entity endedAt is null`() {
        val focusSessionEntity = buildSessionEntity()

        val focusSession = focusSessionEntity.toFocusSession()

        assertNull(focusSession.endedAt)
    }

    @Test
    fun `maps endedAt correctly when entity endedAt is not null`() {
        val focusSessionEntity = buildSessionEntity(endedAtEpochSecond = 1500L)

        val focusSession = focusSessionEntity.toFocusSession()

        assertEquals(Instant.ofEpochSecond(1500L), focusSession.endedAt)
    }

    @Test
    fun `maps durationSeconds correctly`() {
        val focusSessionEntity = buildSessionEntity()

        val focusSession = focusSessionEntity.toFocusSession()

        assertEquals(500L, focusSession.durationInSeconds)
    }

    @Test
    fun `maps label correctly`() {
        val focusSessionEntity = buildSessionEntity()

        val focusSession = focusSessionEntity.toFocusSession()

        assertEquals("test", focusSession.label)
    }
}