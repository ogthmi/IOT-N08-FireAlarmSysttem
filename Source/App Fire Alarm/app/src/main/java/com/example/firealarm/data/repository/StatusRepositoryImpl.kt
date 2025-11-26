package com.example.firealarm.data.repository

import com.example.firealarm.data.api.ApiService
import com.example.firealarm.data.model.ControlRequest
import com.example.firealarm.data.model.SensorDto
import com.example.firealarm.data.model.StatusDto
import com.example.firealarm.domain.model.Sensor
import com.example.firealarm.domain.model.Status
import com.example.firealarm.domain.repository.SensorRepository
import com.example.firealarm.domain.repository.StatusRepository
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

class StatusRepositoryImpl @Inject constructor(
    @Named("statusRef") private val statusRef: DatabaseReference,
    private val apiService: ApiService
) : StatusRepository{
    private var listener: ValueEventListener? = null
    private val TAG = "StatusRepository"
    var lastStatus: Status? = null
    override fun getStatusData(): Flow<Status> = flow{
        while (true) {
            val snapshot = statusRef.limitToLast(1).get().await()
            snapshot.children.lastOrNull()?.getValue(StatusDto::class.java)?.let {
                emit(it.toDomain())
            }
            delay(2000) // Lấy mỗi 2 giây
        }
    }.flowOn(Dispatchers.IO)
    
    override suspend fun setBuzzerState(state: String, deviceId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = ControlRequest(
                device_id = deviceId,
                state = state
            )
            Log.d(TAG, "Sending buzzer control request: deviceId=$deviceId, state=$state")
            val response = apiService.sendBuzzerControl(
                topic = "iot/control/buzzer",
                request = request
            )
            if (response.code == 200) {
                Log.d(TAG, "Buzzer state updated successfully: $state - ${response.message}")
                Result.success(Unit)
            } else {
                Log.e(TAG, "Failed to update buzzer state. Code: ${response.code}, Message: ${response.message}")
                Result.failure(Exception("API returned code ${response.code}: ${response.message}"))
            }
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Timeout connecting to server for buzzer control: ${e.message}", e)
            Result.failure(e)
        } catch (e: java.net.ConnectException) {
            Log.e(TAG, "Cannot connect to server for buzzer control: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating buzzer state: ${e.javaClass.simpleName} - ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun setPumpState(state: String, deviceId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = ControlRequest(
                device_id = deviceId,
                state = state
            )
            Log.d(TAG, "Sending pump control request: deviceId=$deviceId, state=$state")
            val response = apiService.sendPumpControl(
                topic = "iot/control/pump",
                request = request
            )
            if (response.code == 200) {
                Log.d(TAG, "Pump state updated successfully: $state - ${response.message}")
                Result.success(Unit)
            } else {
                Log.e(TAG, "Failed to update pump state. Code: ${response.code}, Message: ${response.message}")
                Result.failure(Exception("API returned code ${response.code}: ${response.message}"))
            }
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "Timeout connecting to server for pump control: ${e.message}", e)
            Result.failure(e)
        } catch (e: java.net.ConnectException) {
            Log.e(TAG, "Cannot connect to server for pump control: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating pump state: ${e.javaClass.simpleName} - ${e.message}", e)
            Result.failure(e)
        }
    }
}