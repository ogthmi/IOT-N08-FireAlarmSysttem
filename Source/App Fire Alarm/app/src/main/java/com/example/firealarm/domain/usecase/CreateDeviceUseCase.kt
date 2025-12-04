package com.example.firealarm.domain.usecase

import com.example.firealarm.domain.repository.DeviceRepository
import com.example.firealarm.presentation.utils.NetworkState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CreateDeviceUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository
) {
    suspend fun execute(
        deviceId: String,
        deviceName: String,
        description: String?,
        userId: Int,
        smokeThreshold: Double,
        fireThreshold: Double
    ): Flow<NetworkState> {
        return deviceRepository.createDevice(
            deviceId = deviceId,
            deviceName = deviceName,
            des = description,
            userId = userId,
            smokeThreshold = smokeThreshold,
            fireThreshold = fireThreshold
        )
    }
}

