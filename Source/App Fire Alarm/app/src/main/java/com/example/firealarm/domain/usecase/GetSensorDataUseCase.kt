package com.example.firealarm.domain.usecase

import com.example.firealarm.domain.model.Sensor
import com.example.firealarm.domain.repository.SensorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSensorDataUseCase @Inject constructor(
    private val repository: SensorRepository
){
    fun execute(): Flow<Sensor> = repository.getSensorData()
}