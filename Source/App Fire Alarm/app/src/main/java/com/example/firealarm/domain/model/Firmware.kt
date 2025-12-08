package com.example.firealarm.domain.model

data class Firmware(
    val id: Int,
    val version: String,
    val versionNumber: Int,
    val releasedAt: String,
    val downloadUrl: String?,
    val description: String?
)

