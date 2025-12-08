package com.example.firealarm.data.model.firmware

data class UpdateFirmwareRequest(
    val version: String,
    val versionNumber: Int,
    val description: String
)

