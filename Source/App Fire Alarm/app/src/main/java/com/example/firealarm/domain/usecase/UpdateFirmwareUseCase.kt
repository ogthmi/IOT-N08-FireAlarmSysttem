package com.example.firealarm.domain.usecase

import com.example.firealarm.domain.repository.FirmwareRepository
import com.example.firealarm.presentation.utils.NetworkState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateFirmwareUseCase @Inject constructor(
    private val firmwareRepository: FirmwareRepository
) {
    suspend fun execute(
        id: Int,
        version: String,
        versionNumber: Int,
        description: String
    ): Flow<NetworkState> {
        return firmwareRepository.updateFirmware(id, version, versionNumber, description)
    }
}

