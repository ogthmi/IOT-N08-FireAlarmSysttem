package com.example.firealarm.domain.usecase

import com.example.firealarm.domain.model.Threshold
import com.example.firealarm.domain.repository.ThresholdRepository
import com.example.firealarm.presentation.utils.NetworkState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SetThresholdUseCase @Inject constructor(
    private val thresholdRepository: ThresholdRepository
){
    suspend fun execute(deviceId: String, thresholds: List<Threshold>): Flow<NetworkState> {
        return thresholdRepository.updateThreshold(deviceId = deviceId, thresholds = thresholds)
    }
}