package com.example.firealarm.domain.repository

import com.example.firealarm.domain.model.Threshold
import com.example.firealarm.presentation.utils.NetworkState
import kotlinx.coroutines.flow.Flow

interface ThresholdRepository {
    suspend fun getThreshold(deviceId: String): Flow<NetworkState>
    suspend fun updateThreshold(deviceId: String, thresholds: List<Threshold>): Flow<NetworkState>
}

