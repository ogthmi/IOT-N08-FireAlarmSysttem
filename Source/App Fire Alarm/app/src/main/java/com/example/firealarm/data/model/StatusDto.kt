package com.example.firealarm.data.model

import com.example.firealarm.domain.model.Status

class StatusDto(
    val buzzer_state: String = "",
    val pump_state: String = "",
    val timestamp: String = "",
    val device_id: String? = ""
){
    fun toDomain(): Status = Status(
        buzzerState = buzzer_state ?: "",
        pumpState = pump_state ?: "",
        time = timestamp ?: "",
        deviceId = device_id ?: ""
    )
}