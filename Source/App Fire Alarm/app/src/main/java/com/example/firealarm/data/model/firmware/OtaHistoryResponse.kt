package com.example.firealarm.data.model.firmware

import com.example.firealarm.domain.model.OtaHistory

data class OtaHistoryResponse(
    val code: Int,
    val message: String,
    val result: OtaHistoryResult
)

data class OtaHistoryResult(
    val content: List<OtaHistoryDto>
)

data class OtaHistoryDto(
    val id: Int,
    val deviceId: String,
    val previousVersion: String?,
    val nextVersion: String,
    val status: String,
    val startTime: String,
    val endTime: String?,
    val progress: Int?
)

fun OtaHistoryDto.toDomain(): OtaHistory {
    return OtaHistory(
        id = id,
        deviceId = deviceId,
        previousVersion = previousVersion,
        nextVersion = nextVersion,
        status = status,
        startTime = startTime,
        endTime = endTime,
        progress = progress
    )
}

