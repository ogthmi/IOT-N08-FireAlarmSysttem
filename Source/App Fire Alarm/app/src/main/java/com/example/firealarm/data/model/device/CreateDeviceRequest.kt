package com.example.firealarm.data.model.device

import com.example.firealarm.data.model.threshold.ThresholdDto

data class CreateDeviceRequest(
    val deviceId: String,
    val deviceName: String,
    val description: String?,
    val userId: Int,
    val sensors: List<SensorDto>,
    val thresholds: List<ThresholdDto>
)

