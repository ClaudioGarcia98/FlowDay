package dev.flowday.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.flowday.core.database.dao.HabitDao
import dev.flowday.core.database.entity.HabitCheckInEntity
import dev.flowday.core.database.entity.HabitEntity
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HabitDaoTest {

    private lateinit var database: FlowDayDatabase
    private lateinit var habitDao: HabitDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FlowDayDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        habitDao = database.habitDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun getHabitsStream_returnsEmptyIfNoHabits() = runTest {
        val result = habitDao.getHabitsStream().first()
        assertEquals(0, result.size)
    }

    @Test
    fun getHabitsStream_returnsListOfHabits() = runTest {
        val entity = buildHabit(createdAt = 1000L)
        val id = habitDao.insertHabit(entity)
        val data = habitDao.getHabitsStream().first()
        val habit = data.find { it.id == id }

        assertEquals(id, habit?.id)
        assertEquals("test", habit?.name)
    }

    @Test
    fun getCheckInsForHabit_returnsEmptyIfNoCheckIns() = runTest {
        val entity = buildHabit(createdAt = 1000L)
        val id = habitDao.insertHabit(entity)

        val result = habitDao.getCheckInsForHabit(id).first()

        assertEquals(0, result.size)
    }

    @Test
    fun getCheckInsForHabit_returnsListOfCheckInsByHabit() = runTest {
        val habitEntity = buildHabit(createdAt = 1000L)
        val id = habitDao.insertHabit(habitEntity)

        val checkInEntity = buildHabitCheckIn(habitId = id, completedAtEpochSecond = 1000L)
        habitDao.insertCheckIn(checkInEntity)


        val habitEntity2 = buildHabit(createdAt = 1000L)
        val id2 = habitDao.insertHabit(habitEntity2)

        val checkInEntity2 = buildHabitCheckIn(habitId = id2, completedAtEpochSecond = 1000L)
        habitDao.insertCheckIn(checkInEntity2)

        val data = habitDao.getCheckInsForHabit(id).first()
        val checkIns = data.find { it.habitId == id }

        assertEquals(id, checkIns?.habitId)
        assertEquals(1, data.size)
        assertEquals("2012-05-27", checkIns?.dateIso)
    }

    @Test
    fun getCheckInsForDate_returnsEmptyIfNoCheckIns() = runTest {
        val result = habitDao.getCheckInsForDate("2012-05-27").first()

        assertEquals(0, result.size)
    }

    @Test
    fun getCheckInsForDate_returnsListOfCheckInsByDate() = runTest {
        val habitEntity = buildHabit(createdAt = 1000L)
        val id = habitDao.insertHabit(habitEntity)

        val checkInEntity = buildHabitCheckIn(
            habitId = id,
            dateIso = "2012-05-27",
            completedAtEpochSecond = 1000L
        )
        habitDao.insertCheckIn(checkInEntity)


        val habitEntity2 = buildHabit(createdAt = 1000L)
        val id2 = habitDao.insertHabit(habitEntity2)

        val checkInEntity2 =
            buildHabitCheckIn(habitId = id2, dateIso = "2012-06-27", completedAtEpochSecond = 1000L)
        habitDao.insertCheckIn(checkInEntity2)

        val data = habitDao.getCheckInsForDate("2012-05-27").first()
        val checkIns = data.find { it.habitId == id }

        assertEquals(id, checkIns?.habitId)
        assertEquals(1, data.size)
        assertEquals("2012-05-27", checkIns?.dateIso)
    }

    @Test
    fun insertHabit_checkIfInsertsCorrectly() = runTest {
        val entity = buildHabit(createdAt = 1000L)
        val id = habitDao.insertHabit(entity)
        assertTrue(id > 0)
    }

    @Test
    fun insertHabit_checkReplaceExistingHabit() = runTest {
        val entity = buildHabit(id = 1, createdAt = 1000L)
        val entity2 = buildHabit(id = 1, name = "test2", createdAt = 2000L)
        habitDao.insertHabit(entity)
        val id2 = habitDao.insertHabit(entity2)
        val data = habitDao.getHabitsStream().first()
        val habit = data.find { it.id == id2 }

        assertEquals(1, data.size)
        assertEquals(1L, habit?.id)
        assertEquals("test2", habit?.name)
    }

    @Test
    fun deleteHabit_removesHabitFromDatabase() = runTest {

        val entity = buildHabit(createdAt = 1000L)
        val id = habitDao.insertHabit(entity)
        habitDao.deleteHabit(id)
        val data = habitDao.getHabitsStream().first()
        val session = data.find { it.id == id }

        assertNull(session)
    }

    @Test
    fun deleteHabitCheckIn_CheckIfDeleteHabitCascadeDeleteCheckIn() = runTest {

        val entity = buildHabit(createdAt = 1000L)
        val id = habitDao.insertHabit(entity)

        val entity2 = buildHabitCheckIn(habitId = id, completedAtEpochSecond = 1000L)
        habitDao.insertCheckIn(entity2)

        habitDao.deleteHabit(id)

        val habitData = habitDao.getHabitsStream().first()
        val habit = habitData.find { it.id == id }

        val habitCheckIData = habitDao.getCheckInsForHabit(id).first()


        assertNull(habit)
        assertEquals(0, habitCheckIData.size)
    }

    @Test
    fun undoCheckIn_checkIfCheckInWasDeleted() = runTest {
        val entity = buildHabit(createdAt = 1000L)
        val id = habitDao.insertHabit(entity)

        val entity2 = buildHabitCheckIn(id, completedAtEpochSecond = 1200L)
        habitDao.insertCheckIn(entity2)

        habitDao.undoCheckIn(id, "2012-05-27")

        val habitCheckIData = habitDao.getCheckInsForHabit(id).first()

        assertTrue(habitCheckIData.isEmpty())
    }

    @Test
    fun insertCheckIn_checkIfInsertsCorrectly() = runTest {
        val entity = buildHabit(createdAt = 1000L)
        val id = habitDao.insertHabit(entity)

        val entity2 = buildHabitCheckIn(id, completedAtEpochSecond = 1200L)
        habitDao.insertCheckIn(entity2)

        val habitCheckIData = habitDao.getCheckInsForHabit(id).first()
        val habitCheckIn = habitCheckIData.find { it.habitId == id }

        assertEquals(id, habitCheckIn?.habitId)
    }

    @Test
    fun insertCheckIn_checkIfIgnoresDuplicates() = runTest {
        val entity = buildHabit(createdAt = 1000L)
        val id = habitDao.insertHabit(entity)

        val entity2 = buildHabitCheckIn(id, completedAtEpochSecond = 1200L)
        habitDao.insertCheckIn(entity2)

        val entity3 = buildHabitCheckIn(id, completedAtEpochSecond = 1200L)
        habitDao.insertCheckIn(entity3)

        val habitCheckIData = habitDao.getCheckInsForHabit(id).first()


        assertEquals(1, habitCheckIData.size)
    }

    private fun buildHabit(
        id: Long = 0,
        name: String = "test",
        iconKey: String = "book",
        createdAt: Long
    ) = HabitEntity(
        id = id,
        name = name,
        iconKey = iconKey,
        createdAtEpochSecond = createdAt
    )

    private fun buildHabitCheckIn(
        habitId: Long,
        dateIso: String = "2012-05-27",
        completedAtEpochSecond: Long,
    ) = HabitCheckInEntity(
        habitId = habitId,
        dateIso = dateIso,
        completedAtEpochSecond = completedAtEpochSecond
    )
}