package com.example.firealarm.presentation.ui.admin.firmware.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firealarm.domain.usecase.UploadFirmwareUseCase
import com.example.firealarm.domain.usecase.UpdateFirmwareUseCase
import com.example.firealarm.presentation.utils.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CreateFirmwareViewModel @Inject constructor(
    private val uploadFirmwareUseCase: UploadFirmwareUseCase,
    private val updateFirmwareUseCase: UpdateFirmwareUseCase
): ViewModel() {
    
    private val _uploadState = MutableStateFlow<NetworkState>(NetworkState.Init)
    val uploadState: StateFlow<NetworkState> = _uploadState.asStateFlow()
    
    fun uploadFirmware(
        file: File,
        version: String,
        versionNumber: Int,
        description: String
    ) {
        viewModelScope.launch {
            _uploadState.value = NetworkState.Loading
            uploadFirmwareUseCase.execute(file, version, versionNumber, description).collect { state ->
                _uploadState.value = state
            }
        }
    }

    fun updateFirmware(
        id: Int,
        version: String,
        versionNumber: Int,
        description: String
    ) {
        viewModelScope.launch {
            _uploadState.value = NetworkState.Loading
            updateFirmwareUseCase.execute(id, version, versionNumber, description).collect { state ->
                _uploadState.value = state
            }
        }
    }
}