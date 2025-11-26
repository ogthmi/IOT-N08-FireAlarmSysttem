package com.example.firealarm.domain.repository

import com.example.firealarm.domain.model.Status
import kotlinx.coroutines.flow.Flow

interface StatusRepository {
    fun getStatusData() : Flow<Status>
    suspend fun setBuzzerState(state: String, deviceId: String): Result<Unit>
    suspend fun setPumpState(state: String, deviceId: String): Result<Unit>
}