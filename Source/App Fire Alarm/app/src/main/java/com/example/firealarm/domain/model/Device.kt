package com.example.firealarm.domain.model

data class Device(
    val deviceId: String,
    val deviceName: String,
    val description: String,
    val userId: Int,
    val sensors: List<Sensor>,
    val thresholds: List<Threshold>
)

data class Sensor(
    val name: String,
    val unit: String?
)
