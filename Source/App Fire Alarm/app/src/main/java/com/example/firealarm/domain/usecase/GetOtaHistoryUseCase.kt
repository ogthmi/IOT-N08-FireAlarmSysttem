package com.example.firealarm.domain.usecase

import com.example.firealarm.domain.repository.FirmwareRepository
import com.example.firealarm.presentation.utils.NetworkState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOtaHistoryUseCase @Inject constructor(
    private val firmwareRepository: FirmwareRepository
) {
    suspend fun execute(deviceId: String): Flow<NetworkState> {
        return firmwareRepository.getOtaHistory(deviceId)
    }
}

