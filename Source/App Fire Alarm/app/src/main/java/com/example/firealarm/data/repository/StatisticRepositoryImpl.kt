package com.example.firealarm.data.repository

import android.util.Log
import com.example.firealarm.data.api.ApiService
import com.example.firealarm.data.model.statistic.toDomain
import com.example.firealarm.data.model.statistic.toDomain as notificationToDomain
import com.example.firealarm.domain.repository.StatisticRepository
import com.example.firealarm.presentation.utils.NetworkState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class StatisticRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : StatisticRepository {

    companion object {
        private const val TAG = "StatisticRepository"
    }

    override suspend fun getSensorStatistics(deviceId: String): Flow<NetworkState> = callbackFlow {
        trySend(NetworkState.Loading)
        try {
            Log.d(TAG, "Fetching sensor statistics for device: $deviceId")
            val response = apiService.getSensorStatistics(deviceId)

            if (response.code == 200 && response.result != null) {
                Log.d(TAG, "Successfully fetched sensor statistics. Temperatures: ${response.result.temperatures.size}, Smokes: ${response.result.smokes.size}, Humidities: ${response.result.humidities.size}")
                trySend(NetworkState.Success(response))
            } else {
                Log.e(TAG, "Failed to fetch sensor statistics. Code: ${response.code}, Message: ${response.message}")
                trySend(NetworkState.Error("Failed to fetch sensor statistics: ${response.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching sensor statistics: ${e.message}", e)
            trySend(NetworkState.Error(e.message ?: "Lỗi khi lấy thống kê cảm biến"))
        }
        awaitClose { }
    }

    override suspend fun getNotificationStatistics(deviceId: String): Flow<NetworkState> = callbackFlow {
        trySend(NetworkState.Loading)
        try {
            Log.d(TAG, "Fetching notification statistics for device: $deviceId")
            val response = apiService.getNotificationStatistics(deviceId)

            if (response.code == 200 && response.result != null) {
                Log.d(TAG, "Successfully fetched notification statistics. Fire: ${response.result.fireNotifications.size}, Smoke: ${response.result.smokeNotifications.size}")
                trySend(NetworkState.Success(response))
            } else {
                Log.e(TAG, "Failed to fetch notification statistics. Code: ${response.code}, Message: ${response.message}")
                trySend(NetworkState.Error("Failed to fetch notification statistics: ${response.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching notification statistics: ${e.message}", e)
            trySend(NetworkState.Error(e.message ?: "Lỗi khi lấy thống kê thông báo"))
        }
        awaitClose { }
    }
}

