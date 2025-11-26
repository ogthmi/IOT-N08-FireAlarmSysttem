package com.example.firealarm.domain.repository

import androidx.lifecycle.LiveData
import com.example.firealarm.domain.model.Sensor
import kotlinx.coroutines.flow.Flow

interface SensorRepository {
    fun getSensorData (): Flow<Sensor>
}