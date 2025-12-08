package com.example.firealarm.domain.usecase

import com.example.firealarm.domain.repository.StatisticRepository
import com.example.firealarm.presentation.utils.NetworkState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSensorStatisticsUseCase @Inject constructor(
    private val statisticRepository: StatisticRepository
) {
    suspend fun execute(deviceId: String): Flow<NetworkState> {
        return statisticRepository.getSensorStatistics(deviceId)
    }
}

