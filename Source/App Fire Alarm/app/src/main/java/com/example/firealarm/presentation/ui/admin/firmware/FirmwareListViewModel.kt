package com.example.firealarm.presentation.ui.admin.firmware

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firealarm.domain.usecase.GetFirmwareListUseCase
import com.example.firealarm.domain.usecase.DeleteFirmwareUseCase
import com.example.firealarm.presentation.utils.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FirmwareListViewModel @Inject constructor(
    private val getFirmwareListUseCase: GetFirmwareListUseCase,
    private val deleteFirmwareUseCase: DeleteFirmwareUseCase
): ViewModel() {

    private val _firmwareState = MutableStateFlow<NetworkState>(NetworkState.Init)
    val firmwareState: StateFlow<NetworkState> = _firmwareState.asStateFlow()

    private val _deleteState = MutableStateFlow<NetworkState>(NetworkState.Init)
    val deleteState: StateFlow<NetworkState> = _deleteState.asStateFlow()

    fun loadFirmwareList() {
        viewModelScope.launch {
            _firmwareState.value = NetworkState.Loading
            getFirmwareListUseCase.execute().collect { state ->
                _firmwareState.value = state
            }
        }
    }

    fun deleteFirmware(id: Int) {
        viewModelScope.launch {
            _deleteState.value = NetworkState.Loading
            deleteFirmwareUseCase.execute(id).collect { state ->
                _deleteState.value = state
            }
        }
    }
}