package com.example.firealarm.domain.usecase

import com.example.firealarm.domain.repository.ThresholdRepository
import com.example.firealarm.presentation.utils.NetworkState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetThresholdUseCase @Inject constructor(
    private val thresholdRepository: ThresholdRepository
) {
    suspend fun execute(deviceId: String): Flow<NetworkState> {
        return thresholdRepository.getThreshold(deviceId)
    }
}