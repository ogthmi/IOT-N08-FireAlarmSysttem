package com.example.firealarm.presentation.ui.admin.device.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firealarm.domain.model.Threshold
import com.example.firealarm.domain.usecase.CreateDeviceUseCase
import com.example.firealarm.domain.usecase.SetThresholdUseCase
import com.example.firealarm.domain.usecase.UpdateDeviceUseCase
import com.example.firealarm.presentation.utils.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateDeviceViewModel @Inject constructor(
    private val createDeviceUseCase: CreateDeviceUseCase,
    private val updateDeviceUseCase: UpdateDeviceUseCase,
    private val setThresholdUseCase: SetThresholdUseCase
) : ViewModel() {
    
    private val _createState = MutableStateFlow<NetworkState>(NetworkState.Init)
    val createState: StateFlow<NetworkState> = _createState.asStateFlow()
    private val _updateState = MutableStateFlow<NetworkState>(NetworkState.Init)
    val updateState: StateFlow<NetworkState> = _updateState.asStateFlow()
    
    fun createDevice(
        deviceId: String,
        deviceName: String,
        description: String?,
        userId: Int,
        smokeThreshold: Double,
        fireThreshold: Double
    ) {
        viewModelScope.launch {
            _createState.value = NetworkState.Loading
            createDeviceUseCase.execute(
                deviceId = deviceId,
                deviceName = deviceName,
                description = description,
                userId = userId,
                smokeThreshold = smokeThreshold,
                fireThreshold = fireThreshold
            ).collect { state ->
                _createState.value = state
            }
        }
    }
    
    fun updateDevice(
        deviceId: String,
        deviceName: String,
        description: String?,
        smokeThreshold: Double,
        fireThreshold: Double
    ) {
        viewModelScope.launch {
            _createState.value = NetworkState.Loading
            // Update device info first
            updateDeviceUseCase.execute(deviceId, deviceName, description).collect { state ->
                when (state) {
                    is NetworkState.Loading, is NetworkState.Init -> {}
                    is NetworkState.Success<*> -> {
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
                        setThresholdUseCase.execute(deviceId, thresholds).collect { thresholdState ->
                            _createState.value = thresholdState
                        }
                    }
                    is NetworkState.Error -> {
                        _createState.value = state
                    }
                }
            }
        }
    }
}