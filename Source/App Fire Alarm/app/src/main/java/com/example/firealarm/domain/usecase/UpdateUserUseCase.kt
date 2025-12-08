package com.example.firealarm.domain.usecase

import com.example.firealarm.domain.repository.UserRepository
import com.example.firealarm.presentation.utils.NetworkState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend fun execute(phoneNumber: String): Flow<NetworkState> {
        return userRepository.updateUser(phoneNumber)
    }
}

