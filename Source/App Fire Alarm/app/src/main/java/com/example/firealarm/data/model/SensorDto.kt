package com.example.firealarm.data.model

import com.example.firealarm.domain.model.Sensor

data class SensorDto(
    val temperature: Float? = 0f,
    val humidity: Float? = 0f,
    val fire_value: Int? = 0,
    val smoke_value: Int? = 0,
    val fire_detected: Boolean? = false,
    val smoke_detected: Boolean? = false,
    val device_id: String? = ""
){
    fun toDomain(): Sensor = Sensor(
        temperature = temperature ?: 0f,
        humidity = humidity ?: 0f,
        fire = fire_value ?: 0,
        smoke = smoke_value ?: 0,
        isFire = fire_detected ?: false,
        isSmoke = smoke_detected ?: false,
        deviceId = device_id ?: ""
    )
}