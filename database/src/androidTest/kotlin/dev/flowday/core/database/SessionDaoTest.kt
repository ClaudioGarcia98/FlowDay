package dev.flowday.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.flowday.core.database.dao.SessionDao
import dev.flowday.core.database.entity.FocusSessionEntity
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class SessionDaoTest {

    private lateinit var database: FlowDayDatabase
    private lateinit var sessionDao: SessionDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FlowDayDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        sessionDao = database.sessionDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertSession_returnsGeneratedId() = runTest {
        val entity = buildSession(startedAt = 1000L)
        val id = sessionDao.insertSession(entity)
        assertTrue(id > 0)
    }

    @Test
    fun insertSession_persistsAllFields() = runTest {
        val entity = buildSession(startedAt = 1000L)

        val id = sessionDao.insertSession(entity)
        val data = sessionDao.getSessionsStream().first()
        val session = data.find { it.id == id }

        assertNotNull(session)
        assertNull(session?.endedAtEpochSecond)
        assertEquals(1000L, session?.startedAtEpochSecond)
        assertEquals("Test", session?.label)
    }

    @Test
    fun getActiveSession_whenNoSessions_returnsNull() = runTest {
        val data = sessionDao.getActiveSession()
        assertNull(data)
    }

    @Test
    fun getActiveSession_whenActiveSessionExists_returnsIt() = runTest {

        val entity = buildSession(startedAt = 1000L)
        val id = sessionDao.insertSession(entity)
        val data = sessionDao.getActiveSession()

        assertNotNull(data)
        assertEquals(id, data?.id)
    }


    @Test
    fun getActiveSession_whenSessionIsEnded_returnsNull() = runTest {

        val entity = buildSession(startedAt = 1000L, endedAt = 2000L)
        sessionDao.insertSession(entity)
        val data = sessionDao.getActiveSession()

        assertNull(data)
    }

    @Test
    fun deleteSession_removesSessionFromDatabase() = runTest {

        val entity = buildSession(startedAt = 1000L, endedAt = 2000L)
        val id = sessionDao.insertSession(entity)
        sessionDao.deleteSession(id)
        val data = sessionDao.getSessionsStream().first()
        val session = data.find { it.id == id }

        assertNull(session)
    }

    @Test
    fun getActiveSession_withMultipleSessions_returnsOnlyActive() = runTest {


        val entity = buildSession(startedAt = 1000L, endedAt = 2000L)
        val entity2 = buildSession(startedAt = 1200L)

        sessionDao.insertSession(entity)
        val id2 = sessionDao.insertSession(entity2)

        val data = sessionDao.getActiveSession()

        assertNotNull(data)
        assertEquals(id2, data?.id)
    }

    @Test
    fun endSession_setsEndedAtCorrectly() = runTest {

        val entity = buildSession(startedAt = 1000L)
        val id = sessionDao.insertSession(entity)
        sessionDao.endSession(id, 2000L)
        val data = sessionDao.getSessionsStream().first()
        val session = data.find { it.id == id }

        assertEquals(2000L, session?.endedAtEpochSecond)
    }

    @Test
    fun getTodaySessionStream_returnsOnlySessionsFromToday() = runTest {

        val entity = buildSession(startedAt = 900L)
        val entity2 = buildSession(startedAt = 1000L)
        sessionDao.insertSession(entity)
        val id2 = sessionDao.insertSession(entity2)

        val data = sessionDao.getTodaySessionStream(1000L).first()
        val session = data.find { it.id == id2 }

        assertEquals(1, data.size)
        assertEquals(1000L, session?.startedAtEpochSecond)
    }


    @Test
    fun getSessionsInRangeStream_returnsOnlySessionsWithinRange() = runTest {

        val entity = buildSession(startedAt = 900L)
        val entity2 = buildSession(startedAt = 1000L)
        val entity3 = buildSession(startedAt = 1200L)
        val entity4 = buildSession(startedAt = 1500L)
        val entity5 = buildSession(startedAt = 2000L)
        sessionDao.insertSession(entity)
        sessionDao.insertSession(entity2)
        sessionDao.insertSession(entity3)
        sessionDao.insertSession(entity4)
        sessionDao.insertSession(entity5)

        val data = sessionDao.getSessionsInRangeStream(1000L, 1500L).first()

        assertEquals(3, data.size)
        assertTrue(data.none { it.startedAtEpochSecond !in 1000L..1500L })
    }

    private fun buildSession(
        startedAt: Long,
        endedAt: Long? = null,
        duration: Long = 0L,
        label: String = "Test"
    ) = FocusSessionEntity(
        startedAtEpochSecond = startedAt,
        endedAtEpochSecond = endedAt,
        durationSeconds = duration,
        label = label
    )
}