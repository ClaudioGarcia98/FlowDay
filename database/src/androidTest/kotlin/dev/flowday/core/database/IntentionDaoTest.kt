package dev.flowday.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.flowday.core.database.dao.IntentionDao
import dev.flowday.core.database.entity.DailyIntentionEntity
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

@RunWith(AndroidJUnit4::class)
class IntentionDaoTest {
    private lateinit var database: FlowDayDatabase
    private lateinit var intentionDao: IntentionDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FlowDayDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        intentionDao = database.intentionDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun upsertIntention_CheckIfNotExistsInsert() = runTest {
        val intentionEntity = buildIntention(createdAtEpochSecond = 1000L)

        intentionDao.upsertIntention(intentionEntity)

        val flowOfIntentionsByDate = intentionDao.getIntentionForDate("2012-05-27").first()

        assertNotNull(flowOfIntentionsByDate)
        assertEquals("2012-05-27", flowOfIntentionsByDate?.dateIso)

    }

    @Test
    fun upsertIntention_CheckIfExistsAndUpsertIt() = runTest {
        val intentionEntity = buildIntention(createdAtEpochSecond = 1000L)
        intentionDao.upsertIntention(intentionEntity)

        val intentionEntity2 = buildIntention(
            eveningReflection = "Bad day",
            createdAtEpochSecond = 1000L
        )
        intentionDao.upsertIntention(intentionEntity2)

        val flowOfIntentionsByDate = intentionDao.getIntentionForDate("2012-05-27").first()

        assertEquals("2012-05-27", flowOfIntentionsByDate?.dateIso)
        assertEquals("Bad day", flowOfIntentionsByDate?.eveningReflection)
    }

    @Test
    fun deleteIntention_removesIntentionFromDatabase() = runTest {

        val entityIntention = buildIntention(createdAtEpochSecond = 1000L)
        intentionDao.upsertIntention(entityIntention)

        intentionDao.deleteIntention(entityIntention)

        val data = intentionDao.getIntentionForDate("2012-05-27").first()

        assertNull(data)
    }

    @Test
    fun getIntentionForDate_returnsNullIfNoIntention() = runTest {
        val result = intentionDao.getIntentionForDate("2012-05-27").first()
        assertNull(result)
    }

    @Test
    fun getIntentionForDate_returnsIntentionByDate() = runTest {
        val intentionEntity = buildIntention(createdAtEpochSecond = 1000L)
        intentionDao.upsertIntention(intentionEntity)

        val intentionEntity2 = buildIntention(dateIso = "2012-06-27", createdAtEpochSecond = 1200L)
        intentionDao.upsertIntention(intentionEntity2)

        val data = intentionDao.getIntentionForDate("2012-05-27").first()

        assertEquals("2012-05-27", data?.dateIso)
    }

    @Test
    fun getIntentionsInRangeStream_returnsOnlyIntentionsWithinRange() = runTest {

        val entity = buildIntention(createdAtEpochSecond = 900L)
        val entity2 = buildIntention(dateIso = "2012-05-29", createdAtEpochSecond = 1000L)
        val entity3 = buildIntention(dateIso = "2012-05-30", createdAtEpochSecond = 1200L)
        val entity4 = buildIntention(dateIso = "2012-05-31", createdAtEpochSecond = 1500L)
        val entity5 = buildIntention(dateIso = "2012-06-01", createdAtEpochSecond = 2000L)
        intentionDao.upsertIntention(entity)
        intentionDao.upsertIntention(entity2)
        intentionDao.upsertIntention(entity3)
        intentionDao.upsertIntention(entity4)
        intentionDao.upsertIntention(entity5)


        val data = intentionDao.getIntentionsInRangeStream("2012-05-30", "2012-06-01").first()

        assertEquals(3, data.size)
        assertTrue(data.none { it.dateIso < "2012-05-30" || it.dateIso > "2012-06-01" })
    }

    private fun buildIntention(
        dateIso: String = "2012-05-27",
        prioritiesJson: String = "[\"workout\", \"read\", \"code\"]",
        eveningReflection: String = "Good day",
        createdAtEpochSecond: Long
    ) = DailyIntentionEntity(
        dateIso = dateIso,
        prioritiesJson = prioritiesJson,
        eveningReflection = eveningReflection,
        createdAtEpochSecond = createdAtEpochSecond
    )
}