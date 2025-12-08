package com.example.firealarm.presentation.ui.user.firmware

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firealarm.domain.usecase.GetFirmwareListUseCase
import com.example.firealarm.domain.usecase.StartOtaUpdateUseCase
import com.example.firealarm.domain.usecase.GetOtaHistoryUseCase
import com.example.firealarm.domain.usecase.CancelOtaUpdateUseCase
import com.example.firealarm.domain.model.OtaHistory
import com.example.firealarm.presentation.utils.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateFirmwareViewModel @Inject constructor(
    private val getFirmwareListUseCase: GetFirmwareListUseCase,
    private val startOtaUpdateUseCase: StartOtaUpdateUseCase,
    private val getOtaHistoryUseCase: GetOtaHistoryUseCase,
    private val cancelOtaUpdateUseCase: CancelOtaUpdateUseCase
): ViewModel() {
    private val _firmwareState = MutableStateFlow<NetworkState>(NetworkState.Init)
    val firmwareState: StateFlow<NetworkState> = _firmwareState.asStateFlow()

    private val _otaUpdateState = MutableStateFlow<NetworkState>(NetworkState.Init)
    val otaUpdateState: StateFlow<NetworkState> = _otaUpdateState.asStateFlow()

    private val _otaHistoryState = MutableStateFlow<NetworkState>(NetworkState.Init)
    val otaHistoryState: StateFlow<NetworkState> = _otaHistoryState.asStateFlow()

    private val _cancelOtaState = MutableStateFlow<NetworkState>(NetworkState.Init)
    val cancelOtaState: StateFlow<NetworkState> = _cancelOtaState.asStateFlow()

    fun loadFirmwareList() {
        viewModelScope.launch {
            _firmwareState.value = NetworkState.Loading
            getFirmwareListUseCase.execute().collect { state ->
                _firmwareState.value = state
            }
        }
    }

    fun startOtaUpdate(deviceId: String, targetVersion: String) {
        viewModelScope.launch {
            _otaUpdateState.value = NetworkState.Loading
            startOtaUpdateUseCase.execute(deviceId, targetVersion).collect { state ->
                _otaUpdateState.value = state
            }
        }
    }

    fun loadOtaHistory(deviceId: String) {
        viewModelScope.launch {
            _otaHistoryState.value = NetworkState.Loading
            getOtaHistoryUseCase.execute(deviceId).collect { state ->
                _otaHistoryState.value = state
            }
        }
    }

    fun cancelOtaUpdate(deviceId: String) {
        viewModelScope.launch {
            _cancelOtaState.value = NetworkState.Loading
            cancelOtaUpdateUseCase.execute(deviceId).collect { state ->
                _cancelOtaState.value = state
            }
        }
    }
}