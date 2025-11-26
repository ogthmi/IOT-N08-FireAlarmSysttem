package com.example.firealarm.domain.model

data class Sensor(
    val temperature: Float = 0f,
    val humidity: Float = 0f,
    val smoke: Int = 0,
    val fire: Int = 0,
    val isFire: Boolean = false,
    val isSmoke: Boolean = false,
    val deviceId: String = ""
)