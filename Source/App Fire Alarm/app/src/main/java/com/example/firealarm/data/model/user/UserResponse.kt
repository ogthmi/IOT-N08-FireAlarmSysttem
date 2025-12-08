package com.example.firealarm.data.model.user

import com.example.firealarm.domain.model.UserInfo

data class UserResponse(
    val code: Int,
    val message: String,
    val result: UserResult?
)

data class UserResult(
    val content: List<UserDto>
)

data class UserDto(
    val id: Int,
    val username: String,
    val phoneNumber: String,
    val role: String
)

fun UserDto.toDomain(): UserInfo {
    return UserInfo(
        id = id,
        username = username,
        phone = phoneNumber,
        deviceCount = 0,
        role = role
    )
}

