package com.example.firealarm.presentation.ui.user.control

import android.util.Log
import android.util.Printer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firealarm.domain.model.Threshold
import com.example.firealarm.domain.repository.ThresholdRepository
import com.example.firealarm.domain.usecase.GetThresholdUseCase
import com.example.firealarm.domain.usecase.SetThresholdUseCase
import com.example.firealarm.presentation.utils.AppPreferences
import com.example.firealarm.presentation.utils.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdjustThresholdViewModel @Inject constructor(
    private val getThresholdUseCase: GetThresholdUseCase,
    private val setThresholdUseCase: SetThresholdUseCase
) : ViewModel() {
    
    private val _thresholdState = MutableStateFlow<NetworkState>(NetworkState.Init)
    val thresholdState: StateFlow<NetworkState> = _thresholdState.asStateFlow()
    
    private val _updateState = MutableStateFlow<NetworkState>(NetworkState.Init)
    val updateState: StateFlow<NetworkState> = _updateState.asStateFlow()

    
    fun loadThresholds() {
        viewModelScope.launch {
            _thresholdState.value = NetworkState.Loading
            val deviceId = AppPreferences.getDeviceId()
            if (deviceId == null) {
                _thresholdState.value = NetworkState.Error("Chưa chọn thiết bị")
                return@launch
            }
            
            // Extract deviceId từ format "device-{id}" nếu có
            val actualDeviceId = if (deviceId.contains("-")) {
                deviceId.split("-")[1].trim()
            } else {
                deviceId
            }

            getThresholdUseCase.execute(actualDeviceId).collect {
                _thresholdState.value = it
            }
        }
    }
    
    fun updateThresholds(fireThreshold: Double, smokeThreshold: Double) {
        viewModelScope.launch {
            val deviceId = AppPreferences.getDeviceId()
            if (deviceId == null) {
                _updateState.value = NetworkState.Error("Chưa chọn thiết bị")
                return@launch
            }
            
            // Extract deviceId từ format "device-{id}" nếu có
            val actualDeviceId = if (deviceId.contains("-")) {
                deviceId.split("-")[1].trim()
            } else {
                deviceId
            }
            
            val thresholds = listOf(
                Threshold(
                    sensorName = "MHS",
                    ruleName = "fire_threshold",
                    threshold = fireThreshold
                ),
                Threshold(
                    sensorName = "MP2",
                    ruleName = "smoke_threshold",
                    threshold = smokeThreshold
                )
            )
            
           setThresholdUseCase.execute(actualDeviceId, thresholds).collect { state ->
                _updateState.value = state
            }
        }
    }
}
