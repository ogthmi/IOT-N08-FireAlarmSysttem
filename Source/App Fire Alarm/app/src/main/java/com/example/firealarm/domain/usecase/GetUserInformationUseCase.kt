package com.example.firealarm.domain.usecase

import com.example.firealarm.domain.repository.UserRepository
import com.example.firealarm.presentation.utils.NetworkState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserInformationUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend fun execute(): Flow<NetworkState> {
        return userRepository.getUserInfor()
    }
}