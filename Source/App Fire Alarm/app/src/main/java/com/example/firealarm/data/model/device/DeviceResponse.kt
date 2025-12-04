package com.example.firealarm.data.model.device

import com.example.firealarm.data.model.threshold.ThresholdDto
import com.example.firealarm.data.model.threshold.toDomain
import com.example.firealarm.domain.model.Device
import com.example.firealarm.domain.model.Sensor

data class DeviceResponse(
    val code: Int,
    val message: String,
    val result: DeviceResult?
)

data class DeviceResult(
    val content: List<DeviceDto>,
)

data class DeviceDto(
    val deviceId: String,
    val deviceName: String,
    val description: String?,
    val userId: Int,
    val sensors: List<SensorDto>,
    val thresholds: List<ThresholdDto>
)

data class SensorDto(
    val name: String,
    val unit: String?
)

fun SensorDto.toDomain(): Sensor {
    return Sensor(
        name = name,
        unit = unit
    )
}

fun DeviceDto.toDomain(): Device {
    return Device(
        deviceId = deviceId,
        deviceName = deviceName,
        description = description ?: "",
        userId = userId,
        sensors = sensors.map { it.toDomain() },
        thresholds = thresholds.map { it.toDomain() }
    )
}

