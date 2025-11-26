package com.example.firealarm.domain.usecase

import com.example.firealarm.domain.model.Sensor
import com.example.firealarm.domain.model.Status
import com.example.firealarm.domain.repository.SensorRepository
import com.example.firealarm.domain.repository.StatusRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStatusDataUseCase @Inject constructor(
    private val repository: StatusRepository
){
    fun execute(): Flow<Status> = repository.getStatusData()
}