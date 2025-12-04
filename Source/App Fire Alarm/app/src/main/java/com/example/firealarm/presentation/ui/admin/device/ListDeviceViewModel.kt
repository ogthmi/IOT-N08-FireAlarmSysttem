package com.example.firealarm.presentation.ui.admin.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firealarm.domain.usecase.DeleteDeviceUseCase
import com.example.firealarm.domain.usecase.GetDevicesByUser
import com.example.firealarm.domain.usecase.GetDevicesUseCase
import com.example.firealarm.presentation.utils.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListDeviceViewModel @Inject constructor(
    private val getDevicesByUser: GetDevicesByUser,
    private val deleteDeviceUseCase: DeleteDeviceUseCase
): ViewModel() {
    private val _deviceState = MutableStateFlow<NetworkState>(NetworkState.Init)
    val deviceState: StateFlow<NetworkState> = _deviceState.asStateFlow()

    private val _deleteState = MutableStateFlow<NetworkState>(NetworkState.Init)
    val deleteState: StateFlow<NetworkState> = _deleteState.asStateFlow()

    fun loadDevices(userId: Int){
        viewModelScope.launch {
            _deviceState.value = NetworkState.Loading
            getDevicesByUser.execute(userId).collect { state ->
                _deviceState.value = state
            }
        }
    }
    
    fun deleteDevice(deviceId: String) {
        viewModelScope.launch {
            _deleteState.value = NetworkState.Loading
            deleteDeviceUseCase.execute(deviceId).collect { state ->
                _deleteState.value = state
            }
        }
    }

}