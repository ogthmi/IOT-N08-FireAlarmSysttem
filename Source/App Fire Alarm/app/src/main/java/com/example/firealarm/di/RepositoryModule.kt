package com.example.firealarm.di

import com.example.firealarm.data.repository.AuthRepositoryImpl
import com.example.firealarm.data.repository.DeviceRepositoryImpl
import com.example.firealarm.data.repository.FirmwareRepositoryImpl
import com.example.firealarm.data.repository.StatusRepositoryImpl
import com.example.firealarm.data.repository.StatisticRepositoryImpl
import com.example.firealarm.data.repository.ThresholdRepositoryImpl
import com.example.firealarm.data.repository.UserRepositoryImpl
import com.example.firealarm.domain.repository.AuthRepository
import com.example.firealarm.domain.repository.DeviceRepository
import com.example.firealarm.domain.repository.FirmwareRepository
import com.example.firealarm.domain.repository.StatusRepository
import com.example.firealarm.domain.repository.StatisticRepository
import com.example.firealarm.domain.repository.ThresholdRepository
import com.example.firealarm.domain.repository.UserRepository
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
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindDeviceRepository(impl: DeviceRepositoryImpl): DeviceRepository

    @Binds
    @Singleton
    abstract fun bindStatusRepository(impl: StatusRepositoryImpl): StatusRepository
    
    @Binds
    @Singleton
    abstract fun bindThresholdRepository(impl: ThresholdRepositoryImpl): ThresholdRepository
    
    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindFirmwareRepository(impl: FirmwareRepositoryImpl): FirmwareRepository

    @Binds
    @Singleton
    abstract fun bindStatisticRepository(impl: StatisticRepositoryImpl): StatisticRepository
}