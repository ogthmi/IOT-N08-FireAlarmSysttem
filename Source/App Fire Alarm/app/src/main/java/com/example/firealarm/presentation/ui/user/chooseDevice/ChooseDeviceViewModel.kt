package com.example.firealarm.presentation.ui.user.chooseDevice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firealarm.domain.model.Device
import com.example.firealarm.domain.usecase.GetDevicesUseCase
import com.example.firealarm.presentation.utils.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChooseDeviceViewModel @Inject constructor(
    private val getDevicesUseCase: GetDevicesUseCase
) : ViewModel() {
    
    private val _devicesState = MutableStateFlow<NetworkState>(NetworkState.Init)
    val devicesState: StateFlow<NetworkState> get() = _devicesState
    
    fun loadDevices() {
        _devicesState.value = NetworkState.Loading
        viewModelScope.launch {
            getDevicesUseCase.execute().collect {
                _devicesState.value = it
            }
        }
    }
}
