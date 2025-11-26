package com.example.firealarm.presentation.ui.control

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firealarm.domain.model.Sensor
import com.example.firealarm.domain.model.Status
import com.example.firealarm.domain.usecase.GetSensorDataUseCase
import com.example.firealarm.domain.usecase.GetStatusDataUseCase
import com.example.firealarm.domain.usecase.SetBuzzerStateUseCase
import com.example.firealarm.domain.usecase.SetPumpStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ControlViewModel @Inject constructor(
    private val getSensorDataUseCase: GetSensorDataUseCase,
    private val getStatusDataUseCase: GetStatusDataUseCase,
    private val setBuzzerStateUseCase: SetBuzzerStateUseCase,
    private val setPumpStateUseCase: SetPumpStateUseCase
): ViewModel() {
    private val _sensorData = MutableStateFlow(Sensor())
    val sensorData: Flow<Sensor> = _sensorData

    private val _statusData = MutableStateFlow(Status())
    val statusData: Flow<Status> = _statusData

    fun getSensor(){
        viewModelScope.launch((Dispatchers.IO)) {
            getSensorDataUseCase.execute()
                .collect { sensors -> _sensorData.value = sensors }
        }
    }

    fun getStatus(){
        viewModelScope.launch((Dispatchers.IO)) {
            getStatusDataUseCase.execute()
                .collect { status -> _statusData.value = status }
        }
    }
    
    fun toggleBuzzer(currentState: String, deviceId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newState = if (currentState == "ON") "OFF" else "ON"
            delay(1000)
            setBuzzerStateUseCase.execute(newState, deviceId).fold(
                onSuccess = {
                    Log.d("---Stattus", "Success")
                },
                onFailure = { error ->
                    // Handle error if needed
                }
            )
        }
    }
    
    fun togglePump(currentState: String, deviceId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newState = if (currentState == "ON") "OFF" else "ON"
            delay(1000)
            setPumpStateUseCase.execute(newState, deviceId).fold(
                onSuccess = {
                    // Success
                },
                onFailure = { error ->
                    // Handle error if needed
                }
            )
        }
    }

}