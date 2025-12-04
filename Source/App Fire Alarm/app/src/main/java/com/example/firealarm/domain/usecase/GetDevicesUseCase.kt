package com.example.firealarm.domain.usecase

import com.example.firealarm.domain.model.Device
import com.example.firealarm.domain.repository.DeviceRepository
import com.example.firealarm.presentation.utils.NetworkState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDevicesUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository
) {
    suspend fun execute(): Flow<NetworkState> {
        return deviceRepository.getDevicesByUser()
    }
}

