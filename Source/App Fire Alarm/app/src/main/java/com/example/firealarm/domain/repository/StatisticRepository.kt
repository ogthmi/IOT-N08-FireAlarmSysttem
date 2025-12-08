package com.example.firealarm.domain.repository

import com.example.firealarm.domain.model.SensorStatistic
import com.example.firealarm.domain.model.NotificationStatistic
import com.example.firealarm.presentation.utils.NetworkState
import kotlinx.coroutines.flow.Flow

interface StatisticRepository {
    suspend fun getSensorStatistics(deviceId: String): Flow<NetworkState>
    suspend fun getNotificationStatistics(deviceId: String): Flow<NetworkState>
}

