package dev.flowday.data.mappersTests

import dev.flowday.data.mapper.toHabit
import dev.flowday.data.mapper.toHabitCheckIn
import dev.flowday.database.entity.HabitCheckInEntity
import dev.flowday.database.entity.HabitEntity
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class HabitMapperTest {

    private fun buildHabitEntity(
        id: Long = 1L,
        name: String = "name test",
        iconKey: String = "icon test",
        createdAtEpochSecond: Long = 1500L
    ) = HabitEntity(
        id = id,
        name = name,
        iconKey = iconKey,
        createdAtEpochSecond = createdAtEpochSecond
    )

    private fun buildHabitCheckInEntity(
        id: Long = 1L,
        habitId: Long = 1L,
        dateIso: String = "2026-05-18",
        completedAtEpochSecond: Long = 1500L,
    ) = HabitCheckInEntity(
        id = id,
        habitId = habitId,
        dateIso = dateIso,
        completedAtEpochSecond = completedAtEpochSecond
    )

    @Test
    fun `maps habitId correctly`() {
        val habitEntity = buildHabitEntity()

        val habit = habitEntity.toHabit()

        assertEquals(1L, habit.id)
    }

    @Test
    fun `maps habitName correctly`() {
        val habitEntity = buildHabitEntity()

        val habit = habitEntity.toHabit()

        assertEquals("name test", habit.name)
    }

    @Test
    fun `maps habitIconKey correctly`() {
        val habitEntity = buildHabitEntity()

        val habit = habitEntity.toHabit()

        assertEquals("icon test", habit.iconKey)
    }

    /* "streaks are never stored, always start at 0".
    If someone changes the mapper in the future and accidentally maps a stored value, the test catches it.*/
    @Test
    fun `maps habitCurrentStreak correctly`() {
        val habitEntity = buildHabitEntity()

        val habit = habitEntity.toHabit()

        assertEquals(0, habit.currentStreak)

    }

    @Test
    fun `maps habitLongestStreak correctly`() {
        val habitEntity = buildHabitEntity()

        val habit = habitEntity.toHabit()

        assertEquals(0, habit.longestStreak)
    }

    @Test
    fun `maps habitCheckInId correctly`() {
        val habitCheckInEntity = buildHabitCheckInEntity()

        val habitCheckIn = habitCheckInEntity.toHabitCheckIn()

        assertEquals(1L, habitCheckIn.id)
    }

    @Test
    fun `maps habitCheckInHabitId correctly`() {
        val habitCheckInEntity = buildHabitCheckInEntity()

        val habitCheckIn = habitCheckInEntity.toHabitCheckIn()

        assertEquals(1L, habitCheckIn.habitId)
    }

    @Test
    fun `maps habitCheckInDate correctly`() {
        val habitCheckInEntity = buildHabitCheckInEntity()

        val habitCheckIn = habitCheckInEntity.toHabitCheckIn()

        assertEquals(LocalDate.parse("2026-05-18"), habitCheckIn.date)
    }

    @Test
    fun `maps habitCheckInCompletedAt correctly`() {
        val habitCheckInEntity = buildHabitCheckInEntity()

        val habitCheckIn = habitCheckInEntity.toHabitCheckIn()

        assertEquals(Instant.ofEpochSecond(1500L), habitCheckIn.completedAt)
    }
}