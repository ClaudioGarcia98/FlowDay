package dev.flowday.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.flowday.data.repository.AnalyticsRepositoryImpl
import dev.flowday.data.repository.HabitRepositoryImpl
import dev.flowday.data.repository.IntentionRepositoryImpl
import dev.flowday.data.repository.SessionRepositoryImpl
import dev.flowday.data.repository.WeatherRepositoryImpl
import dev.flowday.domain.repository.AnalyticsRepository
import dev.flowday.domain.repository.HabitRepository
import dev.flowday.domain.repository.IntentionRepository
import dev.flowday.domain.repository.SessionRepository
import dev.flowday.domain.repository.WeatherRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindSessionRepository(
        impl: SessionRepositoryImpl
    ): SessionRepository

    @Binds
    abstract fun bindIntentionRepository(
        impl: IntentionRepositoryImpl
    ): IntentionRepository

    @Binds
    abstract fun bindHabitRepository(
        impl: HabitRepositoryImpl
    ): HabitRepository

    @Binds
    abstract fun bindAnalyticsRepository(
        impl: AnalyticsRepositoryImpl
    ): AnalyticsRepository

    @Binds
    abstract fun bindWeatherRepository(
        impl: WeatherRepositoryImpl
    ): WeatherRepository
}

