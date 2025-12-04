package com.example.firealarm.domain.usecase

import com.example.firealarm.domain.repository.UserRepository
import com.example.firealarm.presentation.utils.NetworkState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CreateUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend fun execute(username: String, password: String, phone: String): Flow<NetworkState>{
        return userRepository.createUser(username = username, password = password, phone = phone)
    }
}