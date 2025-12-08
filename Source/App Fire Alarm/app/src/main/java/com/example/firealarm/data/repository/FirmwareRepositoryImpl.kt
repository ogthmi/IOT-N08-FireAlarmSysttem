package com.example.firealarm.data.repository

import android.util.Log
import com.example.firealarm.data.api.ApiService
import com.example.firealarm.data.model.firmware.toDomain
import com.example.firealarm.data.model.firmware.OtaHistoryDto
import com.example.firealarm.data.model.firmware.toDomain as otaHistoryToDomain
import com.example.firealarm.domain.repository.FirmwareRepository
import com.example.firealarm.presentation.utils.NetworkState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class FirmwareRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : FirmwareRepository {
    private val TAG = "FirmwareRepository"

    override suspend fun getFirmwareList(): Flow<NetworkState> = callbackFlow {
        trySend(NetworkState.Loading)
        try {
            Log.d(TAG, "Fetching firmware list")
            val response = apiService.getFirmwareList()

            if (response.code == 200 && response.result != null) {
                val firmwareList = response.result.map { it.toDomain() }.sortedByDescending { it.versionNumber } ?: emptyList()
                Log.d(TAG, "Successfully fetched ${firmwareList.size} firmware versions")
                trySend(NetworkState.Success(firmwareList))
            } else {
                Log.e(TAG, "Failed to fetch firmware. Code: ${response.code}, Message: ${response.message}")
                trySend(NetworkState.Error("Failed to fetch firmware: ${response.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching firmware: ${e.message}", e)
            trySend(NetworkState.Error(e.message ?: "Lỗi khi lấy danh sách firmware"))
        }
        awaitClose { }
    }

    override suspend fun uploadFirmware(
        file: File,
        version: String,
        versionNumber: Int,
        description: String
    ): Flow<NetworkState> = callbackFlow {
        trySend(NetworkState.Loading)
        try {
            Log.d(TAG, "Uploading firmware: $version")
            
            val requestFile = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
            
            // Convert string and int to RequestBody
            val versionBody: RequestBody = version.toRequestBody("text/plain".toMediaTypeOrNull())
            val versionNumberBody: RequestBody = versionNumber.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionBody: RequestBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
            
            val response = apiService.uploadFirmware(
                file = filePart,
                version = versionBody,
                versionNumber = versionNumberBody,
                description = descriptionBody
            )

            if (response.code == 200) {
                Log.d(TAG, "Successfully uploaded firmware")
                trySend(NetworkState.Success(Unit))
            } else {
                Log.e(TAG, "Failed to upload firmware. Code: ${response.code}, Message: ${response.message}")
                trySend(NetworkState.Error("Failed to upload firmware: ${response.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading firmware: ${e.message}", e)
            trySend(NetworkState.Error(e.message ?: "Lỗi khi upload firmware"))
        }
        awaitClose { }
    }

    override suspend fun updateFirmware(
        id: Int,
        version: String,
        versionNumber: Int,
        description: String
    ): Flow<NetworkState> = callbackFlow {
        trySend(NetworkState.Loading)
        try {
            Log.d(TAG, "Updating firmware: $id")
            val request = com.example.firealarm.data.model.firmware.UpdateFirmwareRequest(
                version = version,
                versionNumber = versionNumber,
                description = description
            )
            val response = apiService.updateFirmware(id, request)

            if (response.code == 200) {
                Log.d(TAG, "Successfully updated firmware")
                trySend(NetworkState.Success(Unit))
            } else {
                Log.e(TAG, "Failed to update firmware. Code: ${response.code}, Message: ${response.message}")
                trySend(NetworkState.Error("Failed to update firmware: ${response.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating firmware: ${e.message}", e)
            trySend(NetworkState.Error(e.message ?: "Lỗi khi cập nhật firmware"))
        }
        awaitClose { }
    }

    override suspend fun deleteFirmware(id: Int): Flow<NetworkState> = callbackFlow {
        trySend(NetworkState.Loading)
        try {
            Log.d(TAG, "Deleting firmware: $id")
            val response = apiService.deleteFirmware(id)

            if (response.code == 200) {
                Log.d(TAG, "Successfully deleted firmware")
                trySend(NetworkState.Success(Unit))
            } else {
                Log.e(TAG, "Failed to delete firmware. Code: ${response.code}, Message: ${response.message}")
                trySend(NetworkState.Error("Failed to delete firmware: ${response.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting firmware: ${e.message}", e)
            trySend(NetworkState.Error(e.message ?: "Lỗi khi xóa firmware"))
        }
        awaitClose { }
    }

    override suspend fun startOtaUpdate(deviceId: String, targetVersion: String): Flow<NetworkState> = callbackFlow {
        trySend(NetworkState.Loading)
        try {
            Log.d(TAG, "Starting OTA update for device: $deviceId to version: $targetVersion")
            val request = com.example.firealarm.data.model.firmware.OtaUpdateRequest(
                deviceId = deviceId,
                targetVersion = targetVersion
            )
            val response = apiService.startOtaUpdate(request)

            if (response.code == 200) {
                Log.d(TAG, "Successfully started OTA update")
                trySend(NetworkState.Success(Unit))
            } else {
                Log.e(TAG, "Failed to start OTA update. Code: ${response.code}, Message: ${response.message}")
                trySend(NetworkState.Error("Failed to start OTA update: ${response.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting OTA update: ${e.message}", e)
            trySend(NetworkState.Error(e.message ?: "Lỗi khi bắt đầu cập nhật firmware"))
        }
        awaitClose { }
    }

    override suspend fun getOtaHistory(deviceId: String): Flow<NetworkState> = callbackFlow {
        trySend(NetworkState.Loading)
        try {
            Log.d(TAG, "Fetching OTA history for device: $deviceId")
            val response = apiService.getOtaHistory(deviceId)

            if (response.code == 200 && response.result != null) {
                val historyList = response.result.content.map { it.otaHistoryToDomain() }
                Log.d(TAG, "Successfully fetched ${historyList.size} OTA history records")
                trySend(NetworkState.Success(historyList))
            } else {
                Log.e(TAG, "Failed to fetch OTA history. Code: ${response.code}, Message: ${response.message}")
                trySend(NetworkState.Error("Failed to fetch OTA history: ${response.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching OTA history: ${e.message}", e)
            trySend(NetworkState.Error(e.message ?: "Lỗi khi lấy lịch sử OTA"))
        }
        awaitClose { }
    }

    override suspend fun cancelOtaUpdate(deviceId: String): Flow<NetworkState> = callbackFlow {
        trySend(NetworkState.Loading)
        try {
            Log.d(TAG, "Cancelling OTA update for device: $deviceId")
            val response = apiService.cancelOtaUpdate(deviceId)

            if (response.code == 200) {
                Log.d(TAG, "Successfully cancelled OTA update")
                trySend(NetworkState.Success(Unit))
            } else {
                Log.e(TAG, "Failed to cancel OTA update. Code: ${response.code}, Message: ${response.message}")
                trySend(NetworkState.Error("Failed to cancel OTA update: ${response.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling OTA update: ${e.message}", e)
            trySend(NetworkState.Error(e.message ?: "Lỗi khi hủy cập nhật firmware"))
        }
        awaitClose { }
    }
}

