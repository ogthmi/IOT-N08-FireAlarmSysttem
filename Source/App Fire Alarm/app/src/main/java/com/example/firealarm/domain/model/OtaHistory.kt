package com.example.firealarm.domain.model

data class OtaHistory(
    val id: Int,
    val deviceId: String,
    val previousVersion: String?,
    val nextVersion: String,
    val status: String,
    val startTime: String,
    val endTime: String?,
    val progress: Int?
)

