package com.example.firealarm.data.repository

import android.util.Log
import com.example.firealarm.data.api.ApiService
import com.example.firealarm.data.model.auth.toDomain
import com.example.firealarm.data.model.device.CreateDeviceRequest
import com.example.firealarm.data.model.device.UpdateDeviceRequest
import com.example.firealarm.data.model.device.DeviceDto
import com.example.firealarm.data.model.device.SensorDto
import com.example.firealarm.data.model.device.toDomain
import com.example.firealarm.data.model.threshold.ThresholdDto
import com.example.firealarm.domain.repository.DeviceRepository
import com.example.firealarm.presentation.utils.NetworkState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class DeviceRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : DeviceRepository {
    private val TAG = "DeviceRepository"
    
    override suspend fun getDevicesByUser(): Flow<NetworkState> = callbackFlow {
        trySend(NetworkState.Loading)
        try {
            Log.d(TAG, "Fetching devices for user")
            val response = apiService.getDeviceByUser()
            
            if (response.code == 200 && response.result != null) {
                val devices = response.result.content.map { it.toDomain() }.sortedBy { it.deviceId }
                Log.d(TAG, "Successfully fetched ${devices.size} devices")
                trySend(NetworkState.Success(devices))
            } else {
                Log.e(TAG, "Failed to fetch devices. Code: ${response.code}, Message: ${response.message}")
                trySend(NetworkState.Error("Failed to fetch devices: ${response.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching devices: ${e.message}", e)
            trySend(NetworkState.Error(e.message ?: "Lỗi khi lấy danh sách thiết bị"))
        }
        awaitClose {  }
    }

    override suspend fun getDevicesByUserId(userId: Int): Flow<NetworkState> = callbackFlow{
        trySend(NetworkState.Loading)
        try {
            Log.d(TAG, "Fetching devices for user")
            val response = apiService.getAllDevices()

            if (response.code == 200 && response.result != null) {
                var devices = response.result?.content?.filter { deviceDto -> deviceDto.userId == userId }
                val devicesList = devices?.map { it.toDomain() }?.sortedBy { it.deviceId }
                Log.d(TAG, "Successfully fetched ${devicesList?.size} devices")
                trySend(NetworkState.Success(devicesList))
            } else {
                Log.e(TAG, "Failed to fetch devices. Code: ${response.code}, Message: ${response.message}")
                trySend(NetworkState.Error("Failed to fetch devices: ${response.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching devices: ${e.message}", e)
            trySend(NetworkState.Error(e.message ?: "Lỗi khi lấy danh sách thiết bị"))
        }
        awaitClose {  }
    }

    override suspend fun createDevice(
        deviceId: String,
        deviceName: String,
        des: String?,
        userId: Int,
        smokeThreshold: Double,
        fireThreshold: Double
    ): Flow<NetworkState> = callbackFlow {
        trySend(NetworkState.Loading)
        try {
            Log.d(TAG, "Creating device")
            
            val sensors = listOf(
                SensorDto(
                    name = "DHT22T",
                    unit = "°C"
                ),
                SensorDto(
                    name = "DHT22H",
                    unit = "%RH"
                ),
                SensorDto(
                    name = "MP2",
                    unit = null
                ),
                SensorDto(
                    name = "MHS",
                    unit = null
                ),
                SensorDto(
                    name = "BUZZER",
                    unit = null
                ),
                SensorDto(
                    name = "PUMP",
                    unit = null
                )
            )
            
            val thresholds = listOf(
                ThresholdDto(
                    sensorName = "MHS",
                    ruleName = "fire_threshold",
                    threshold = fireThreshold
                ),
                ThresholdDto(
                    sensorName = "MP2",
                    ruleName = "smoke_threshold",
                    threshold = smokeThreshold
                )
            )
            
            val request = CreateDeviceRequest(
                deviceId = deviceId,
                deviceName = deviceName,
                description = des,
                userId = userId,
                sensors = sensors,
                thresholds = thresholds
            )
            
            val response = apiService.createDevice(request)
            
            if (response.code == 200) {
                Log.d(TAG, "Successfully created device")
                trySend(NetworkState.Success(Unit))
            } else {
                Log.e(TAG, "Failed to create device. Code: ${response.code}, Message: ${response.message}")
                trySend(NetworkState.Error("Failed to create device: ${response.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating device: ${e.message}", e)
            trySend(NetworkState.Error(e.message ?: "Lỗi khi tạo thiết bị"))
        }
        awaitClose { }
    }
    
    override suspend fun updateDevice(deviceId: String, deviceName: String, description: String?): Flow<NetworkState> = callbackFlow {
        trySend(NetworkState.Loading)
        try {
            Log.d(TAG, "Updating device: $deviceId")
            val request = UpdateDeviceRequest(
                deviceName = deviceName,
                description = description
            )
            val response = apiService.updateDevice(deviceId, request)
            
            if (response.code == 200) {
                Log.d(TAG, "Successfully updated device")
                trySend(NetworkState.Success(Unit))
            } else {
                Log.e(TAG, "Failed to update device. Code: ${response.code}, Message: ${response.message}")
                trySend(NetworkState.Error("Failed to update device: ${response.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating device: ${e.message}", e)
            trySend(NetworkState.Error(e.message ?: "Lỗi khi cập nhật thiết bị"))
        }
        awaitClose { }
    }
    
    override suspend fun deleteDevice(deviceId: String): Flow<NetworkState> = callbackFlow {
        trySend(NetworkState.Loading)
        try {
            Log.d(TAG, "Deleting device: $deviceId")
            val response = apiService.deleteDevice(deviceId)
            
            if (response.code == 200) {
                Log.d(TAG, "Successfully deleted device")
                trySend(NetworkState.Success(Unit))
            } else {
                Log.e(TAG, "Failed to delete device. Code: ${response.code}, Message: ${response.message}")
                trySend(NetworkState.Error("Failed to delete device: ${response.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting device: ${e.message}", e)
            trySend(NetworkState.Error(e.message ?: "Lỗi khi xóa thiết bị"))
        }
        awaitClose { }
    }
}

