package com.example.firealarm.data.model.user

import com.example.firealarm.presentation.utils.Constant

data class CreateUserRequest(
    val username: String,
    val password: String,
    val phoneNumber: String,
    val role: String = Constant.user
)

