package com.example.firealarm.domain.model

data class Telemetry(
    val deviceId: String,
    val name: String,
    val value: String?,
    val unit: String?,
    val status: Boolean?
)

