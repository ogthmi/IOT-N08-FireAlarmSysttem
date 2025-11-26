package com.example.firealarm.domain.usecase

import com.example.firealarm.domain.repository.StatusRepository
import javax.inject.Inject

class SetBuzzerStateUseCase @Inject constructor(
    private val repository: StatusRepository
) {
    suspend fun execute(state: String, deviceId: String): Result<Unit> = repository.setBuzzerState(state, deviceId)
}

