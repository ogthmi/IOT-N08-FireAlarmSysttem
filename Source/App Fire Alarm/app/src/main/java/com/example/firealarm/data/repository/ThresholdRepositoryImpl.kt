package com.example.firealarm.data.repository

import android.util.Log
import com.example.firealarm.data.api.ApiService
import com.example.firealarm.data.model.threshold.ThresholdDto
import com.example.firealarm.data.model.threshold.toDomain
import com.example.firealarm.domain.model.Threshold
import com.example.firealarm.domain.repository.ThresholdRepository
import com.example.firealarm.presentation.utils.NetworkState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class ThresholdRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ThresholdRepository {
    private val TAG = "ThresholdRepository"
    
    override suspend fun getThreshold(deviceId: String): Flow<NetworkState> = callbackFlow {
        trySend(NetworkState.Loading)
        try {
            Log.d(TAG, "Fetching threshold for device: $deviceId")
            val response = apiService.getThreshold(deviceId)
            
            if (response.code == 200 && response.result != null) {
                val thresholds = response.result.map { it.toDomain() }
                trySend(NetworkState.Success(thresholds))
            } else {
                Log.e(TAG, "Failed to fetch threshold. Code: ${response.code}, Message: ${response.message}")
                trySend(NetworkState.Error("Failed to fetch threshold: ${response.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching threshold: ${e.message}", e)
            trySend(NetworkState.Error(e.message ?: "Lỗi khi lấy ngưỡng"))
        }
        awaitClose { }
    }
    
    override suspend fun updateThreshold(deviceId: String, thresholds: List<Threshold>): Flow<NetworkState> = callbackFlow {
        trySend(NetworkState.Loading)
        try {
            Log.d(TAG, "Updating threshold for device: $deviceId")
            val request = thresholds.map { threshold ->
                ThresholdDto(
                    sensorName = threshold.sensorName,
                    ruleName = threshold.ruleName,
                    threshold = threshold.threshold
                )
            }
            val response = apiService.updateThreshold(deviceId, request)
            
            if (response.code == 200) {
                Log.d(TAG, "Successfully updated threshold")
                trySend(NetworkState.Success(Unit))
            } else {
                Log.e(TAG, "Failed to update threshold. Code: ${response.code}, Message: ${response.message}")
                trySend(NetworkState.Error("Failed to update threshold: ${response.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating threshold: ${e.message}", e)
            trySend(NetworkState.Error(e.message ?: "Lỗi khi cập nhật ngưỡng"))
        }
        awaitClose { }
    }
}

