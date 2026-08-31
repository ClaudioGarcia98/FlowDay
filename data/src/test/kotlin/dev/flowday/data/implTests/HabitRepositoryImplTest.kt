package dev.flowday.data.implTests

import app.cash.turbine.test
import dev.flowday.data.repository.HabitRepositoryImpl
import dev.flowday.database.dao.HabitDao
import dev.flowday.database.entity.HabitCheckInEntity
import dev.flowday.database.entity.HabitEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate

class HabitRepositoryImplTest {

    private val habitDao = mockk<HabitDao>()

    private val repository = HabitRepositoryImpl(habitDao)

    private fun buildHabitEntity(
        id: Long = 1L,
        name: String = "test",
        iconKey: String = "icon Key",
        createdAtEpochSecond: Long = 500L,
    ) = HabitEntity(
        id = id,
        name = name,
        iconKey = iconKey,
        createdAtEpochSecond = createdAtEpochSecond,
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
    fun `getHabitsStream returns mapped habits`() = runTest {

        val habitEntity = buildHabitEntity()

        every { habitDao.getHabitsStream() } returns flowOf(listOf(habitEntity))

        repository.getHabitsStream().test {
            val listOfHabits = awaitItem()
            assertEquals(1, listOfHabits.size)
            assertEquals(1L, listOfHabits.first().id)
            awaitComplete()
        }
    }

    @Test
    fun `getCheckInsForDate returns mapped habitsCheckIn`() = runTest {

        val habitCheckInEntity = buildHabitCheckInEntity()

        every { habitDao.getCheckInsForDate(any()) } returns flowOf(listOf(habitCheckInEntity))

        repository.getCheckInsForDate(LocalDate.now()).test {
            val listOfHabits = awaitItem()
            assertEquals(1, listOfHabits.size)
            assertEquals(1L, listOfHabits.first().id)
            awaitComplete()
        }
    }

    @Test
    fun `getCheckInsForHabit returns mapped habitsCheckIn`() = runTest {

        val habitCheckInEntity = buildHabitCheckInEntity()

        every { habitDao.getCheckInsForHabit(any()) } returns flowOf(listOf(habitCheckInEntity))

        repository.getCheckInsForHabit(1L).test {
            val listOfHabits = awaitItem()
            assertEquals(1, listOfHabits.size)
            assertEquals(1L, listOfHabits.first().id)
            awaitComplete()
        }
    }

    @Test
    fun `createHabit calls dao with correct entity`() = runTest {

        coEvery { habitDao.insertHabit(any()) } returns 1L

        repository.createHabit("test", iconKey = "icon Key")

        coVerify { habitDao.insertHabit(match { it.name == "test" && it.iconKey == "icon Key" }) }
    }

    @Test
    fun `checkIn calls dao with correct entity`() = runTest {

        coEvery { habitDao.insertCheckIn(any()) } returns Unit

        repository.checkIn(habitId = 1L, date = LocalDate.parse("2026-05-18"))

        coVerify { habitDao.insertCheckIn(match { it.habitId == 1L && it.dateIso == "2026-05-18" }) }
    }

    @Test
    fun `undoCheckIn calls dao with correct arguments`() = runTest {
        coEvery { habitDao.undoCheckIn(any(), any()) } returns Unit

        repository.undoCheckIn(habitId = 1L, date = LocalDate.parse("2026-05-18"))

        coVerify { habitDao.undoCheckIn(1L, "2026-05-18") }
    }

    @Test
    fun `deleteHabit calls dao with correct arguments`() = runTest {
        coEvery { habitDao.deleteHabit(any()) } returns Unit

        repository.deleteHabit(habitId = 1L)

        coVerify { habitDao.deleteHabit(1L) }
    }
}