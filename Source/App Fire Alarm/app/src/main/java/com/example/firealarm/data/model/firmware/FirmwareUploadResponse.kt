package com.example.firealarm.data.model.firmware

data class FirmwareUploadResponse(
    val code: Int,
    val message: String,
    val result: FirmwareDto
)

