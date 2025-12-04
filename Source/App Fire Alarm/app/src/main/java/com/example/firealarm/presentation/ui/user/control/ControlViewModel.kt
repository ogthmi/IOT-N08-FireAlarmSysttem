package com.example.firealarm.presentation.ui.user.control

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firealarm.data.websocket.WebViewStompManager
import com.example.firealarm.domain.model.Telemetry
import com.example.firealarm.domain.usecase.SetBuzzerStateUseCase
import com.example.firealarm.domain.usecase.SetPumpStateUseCase
import com.example.firealarm.presentation.utils.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ControlViewModel @Inject constructor(
    private val setBuzzerStateUseCase: SetBuzzerStateUseCase,
    private val setPumpStateUseCase: SetPumpStateUseCase,
    private val webViewStompManager: WebViewStompManager
): ViewModel() {
    
    private val _telemetryData = MutableStateFlow<List<Telemetry>>(emptyList())
    val telemetryData: StateFlow<List<Telemetry>> = _telemetryData.asStateFlow()
    
    private val _connectionStatus = MutableStateFlow(false)
    val connectionStatus: StateFlow<Boolean> = _connectionStatus.asStateFlow()
    
    private var currentDeviceId: String? = null
    
    init {
        setupWebSocket()
    }
    
    private fun setupWebSocket() {
        // Lấy deviceId từ SharedPreferences
        val device = AppPreferences.getDeviceId()
        if(device != null) currentDeviceId = device.split('-')[1].trim()

        webViewStompManager.setTelemetryCallback { telemetryList ->
            // Filter data theo deviceId hiện tại
            val filteredData = telemetryList.filter { it.deviceId == currentDeviceId }
            
            if (filteredData.isNotEmpty()) {
                _telemetryData.value = filteredData
            }
        }
        
        // Setup callback để nhận connection status
        webViewStompManager.setConnectionStatusCallback { isConnected ->
            _connectionStatus.value = isConnected
        }
        
        // Connect WebSocket
        connectWebSocket()
    }
    
    fun connectWebSocket() {
        if (!webViewStompManager.isConnected()) {
            webViewStompManager.connect()
        }
    }
    
    fun disconnectWebSocket() {
        webViewStompManager.disconnect()
        _connectionStatus.value = false
    }
    
    override fun onCleared() {
        super.onCleared()
        disconnectWebSocket()
    }
//    private val _sensorData = MutableStateFlow(Sensor())
//    val sensorData: Flow<Sensor> = _sensorData
//
//    private val _statusData = MutableStateFlow(Status())
//    val statusData: Flow<Status> = _statusData
//
//    fun getSensor(){
//        viewModelScope.launch((Dispatchers.IO)) {
//            getSensorDataUseCase.execute()
//                .collect { sensors -> _sensorData.value = sensors }
//        }
//    }
//
//    fun getStatus(){
//        viewModelScope.launch((Dispatchers.IO)) {
//            getStatusDataUseCase.execute()
//                .collect { status -> _statusData.value = status }
//        }
//    }
//
//    fun toggleBuzzer(currentState: String, deviceId: String) {
//        viewModelScope.launch(Dispatchers.IO) {
//            val newState = if (currentState == "ON") "OFF" else "ON"
//            delay(1000)
//            setBuzzerStateUseCase.execute(newState, deviceId).fold(
//                onSuccess = {
//                    Log.d("---Stattus", "Success")
//                },
//                onFailure = { error ->
//                    // Handle error if needed
//                }
//            )
//        }
//    }
//
//    fun togglePump(currentState: String, deviceId: String) {
//        viewModelScope.launch(Dispatchers.IO) {
//            val newState = if (currentState == "ON") "OFF" else "ON"
//            delay(1000)
//            setPumpStateUseCase.execute(newState, deviceId).fold(
//                onSuccess = {
//                    // Success
//                },
//                onFailure = { error ->
//                    // Handle error if needed
//                }
//            )
//        }
//    }

}