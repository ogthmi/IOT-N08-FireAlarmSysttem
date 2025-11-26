package com.example.firealarm.domain.usecase

import com.example.firealarm.domain.repository.StatusRepository
import javax.inject.Inject

class SetPumpStateUseCase @Inject constructor(
    private val repository: StatusRepository
) {
    suspend fun execute(state: String, deviceId: String): Result<Unit> = repository.setPumpState(state, deviceId)
}

