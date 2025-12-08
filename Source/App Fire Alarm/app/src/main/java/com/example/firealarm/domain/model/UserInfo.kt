package com.example.firealarm.domain.model

data class UserInfo(
    val id: Int,
    val username: String,
    val phone: String,
    val deviceCount: Int,
    val role: String?
)

