package com.example.firealarm.domain.usecase

import com.example.firealarm.domain.repository.StatisticRepository
import com.example.firealarm.presentation.utils.NetworkState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotificationStatisticsUseCase @Inject constructor(
    private val statisticRepository: StatisticRepository
) {
    suspend fun execute(deviceId: String): Flow<NetworkState> {
        return statisticRepository.getNotificationStatistics(deviceId)
    }
}

