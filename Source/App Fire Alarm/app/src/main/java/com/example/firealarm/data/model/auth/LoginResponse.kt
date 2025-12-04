package com.example.firealarm.data.model.auth

import com.example.firealarm.domain.model.User

data class LoginResponse(
    val code: Int,
    val message: String,
    val result: LoginResult?
)

data class LoginResult(
    val accessToken: String,
    val role: String,
)

fun LoginResponse.toDomain(): User? {
    return if (code == 200 && result != null) {
        User(
            accessToken = result.accessToken,
            role = result.role,
        )
    } else {
        null
    }
}

