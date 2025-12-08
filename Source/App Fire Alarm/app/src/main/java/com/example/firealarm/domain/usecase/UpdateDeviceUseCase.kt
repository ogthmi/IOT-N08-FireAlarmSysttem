package com.example.firealarm.domain.usecase

import com.example.firealarm.domain.repository.DeviceRepository
import com.example.firealarm.presentation.utils.NetworkState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateDeviceUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository
) {
    suspend fun execute(
        deviceId: String,
        deviceName: String,
        description: String?
    ): Flow<NetworkState> {
        return deviceRepository.updateDevice(deviceId, deviceName, description)
    }
}

