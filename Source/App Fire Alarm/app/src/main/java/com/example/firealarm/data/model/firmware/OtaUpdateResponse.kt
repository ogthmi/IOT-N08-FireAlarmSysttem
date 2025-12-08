package com.example.firealarm.data.model.firmware

data class OtaUpdateResponse(
    val code: Int,
    val message: String,
    val result: OtaUpdateResult
)

data class OtaUpdateResult(
    val id: Int,
    val deviceId: String,
    val previousVersion: String?,
    val nextVersion: String,
    val status: String,
    val startTime: String,
    val endTime: String?,
    val progress: Int?
)

