package com.example.firealarm.data.model.firmware

import com.example.firealarm.domain.model.Firmware

data class FirmwareResponse(
    val code: Int,
    val message: String,
    val result: List<FirmwareDto>
)

data class FirmwareDto(
    val id: Int,
    val version: String,
    val versionNumber: Int,
    val releasedAt: String,
    val downloadUrl: String?,
    val description: String?
)

fun FirmwareDto.toDomain(): Firmware {
    return Firmware(
        id = id,
        version = version,
        versionNumber = versionNumber,
        releasedAt = releasedAt,
        downloadUrl = downloadUrl,
        description = description
    )
}

