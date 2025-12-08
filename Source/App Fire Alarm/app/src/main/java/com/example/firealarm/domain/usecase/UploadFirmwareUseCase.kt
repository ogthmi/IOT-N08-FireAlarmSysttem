package com.example.firealarm.domain.usecase

import com.example.firealarm.domain.repository.FirmwareRepository
import com.example.firealarm.presentation.utils.NetworkState
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject

class UploadFirmwareUseCase @Inject constructor(
    private val firmwareRepository: FirmwareRepository
) {
    suspend fun execute(
        file: File,
        version: String,
        versionNumber: Int,
        description: String
    ): Flow<NetworkState> {
        return firmwareRepository.uploadFirmware(file, version, versionNumber, description)
    }
}

