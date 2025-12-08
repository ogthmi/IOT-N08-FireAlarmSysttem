package com.example.firealarm.domain.usecase

import com.example.firealarm.domain.repository.AuthRepository
import com.example.firealarm.presentation.utils.NetworkState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend fun execute(username: String, password: String): Flow<NetworkState> {
        return authRepository.login(username, password)
    }
}

