package com.example.firealarm.data.model.telemetry

import com.example.firealarm.domain.model.Telemetry

data class TelemetryResponse(
    val deviceId: String,
    val name: String,
    val value: String?,
    val unit: String?,
    val status: Boolean?
)

fun TelemetryResponse.toDomain(): Telemetry {
    return Telemetry(
        deviceId = deviceId,
        name = name,
        value = value,
        unit = unit,
        status = status
    )
}

