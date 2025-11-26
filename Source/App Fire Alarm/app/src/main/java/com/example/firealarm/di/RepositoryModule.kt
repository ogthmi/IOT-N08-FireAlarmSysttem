package com.example.firealarm.di

import com.example.firealarm.data.repository.SensorRepositoryImpl
import com.example.firealarm.data.repository.StatusRepositoryImpl
import com.example.firealarm.domain.repository.SensorRepository
import com.example.firealarm.domain.repository.StatusRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSensorRepository(impl: SensorRepositoryImpl): SensorRepository

    @Binds
    @Singleton
    abstract fun bindStatusRepository(impl: StatusRepositoryImpl): StatusRepository
}