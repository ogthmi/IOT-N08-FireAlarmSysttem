package com.example.firealarm.domain.repository

import com.example.firealarm.presentation.utils.NetworkState
import kotlinx.coroutines.flow.Flow
import java.io.File

interface FirmwareRepository {
    suspend fun getFirmwareList(): Flow<NetworkState>
    suspend fun uploadFirmware(
        file: File,
        version: String,
        versionNumber: Int,
        description: String
    ): Flow<NetworkState>
    suspend fun updateFirmware(
        id: Int,
        version: String,
        versionNumber: Int,
        description: String
    ): Flow<NetworkState>
    suspend fun deleteFirmware(id: Int): Flow<NetworkState>
    suspend fun startOtaUpdate(deviceId: String, targetVersion: String): Flow<NetworkState>
    suspend fun getOtaHistory(deviceId: String): Flow<NetworkState>
    suspend fun cancelOtaUpdate(deviceId: String): Flow<NetworkState>
}

