package dev.flowday.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.flowday.core.database.FlowDayDatabase
import dev.flowday.core.database.dao.HabitDao
import dev.flowday.core.database.dao.IntentionDao
import dev.flowday.core.database.dao.SessionDao
import javax.inject.Singleton

@Module
@InstallIn(
    SingletonComponent::class
)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FlowDayDatabase =
        Room.databaseBuilder(
            context,
            FlowDayDatabase::class.java,
            "flowday.db"
        ).fallbackToDestructiveMigration(false).build()

    @Provides
    fun provideSessionDao(database: FlowDayDatabase): SessionDao =
        database.sessionDao()

    @Provides
    fun provideHabitDao(database: FlowDayDatabase): HabitDao =
        database.habitDao()

    @Provides
    fun provideIntentionDao(database: FlowDayDatabase): IntentionDao =
        database.intentionDao()
}