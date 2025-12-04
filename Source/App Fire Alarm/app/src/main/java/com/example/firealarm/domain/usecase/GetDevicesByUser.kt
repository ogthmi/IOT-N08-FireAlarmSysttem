package com.example.firealarm.domain.usecase

import com.example.firealarm.domain.repository.DeviceRepository
import javax.inject.Inject

class GetDevicesByUser @Inject constructor(
    private val deviceRepository: DeviceRepository
) {
    suspend fun execute(userId: Int) = deviceRepository.getDevicesByUserId(userId)
}