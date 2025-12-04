package com.example.firealarm.domain.repository

import com.example.firealarm.domain.model.Device
import com.example.firealarm.presentation.utils.NetworkState
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    suspend fun getDevicesByUser(): Flow<NetworkState>
    suspend fun getDevicesByUserId(userId: Int): Flow<NetworkState>
    suspend fun createDevice(deviceId: String, deviceName: String, des: String?, userId: Int, smokeThreshold: Double, fireThreshold: Double): Flow<NetworkState>
    suspend fun updateDevice(deviceId: String, deviceName: String, description: String?): Flow<NetworkState>
    suspend fun deleteDevice(deviceId: String): Flow<NetworkState>
}

