package com.example.firealarm.data.model.firmware

data class OtaUpdateRequest(
    val deviceId: String,
    val targetVersion: String
)

