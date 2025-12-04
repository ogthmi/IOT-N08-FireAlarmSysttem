package com.example.firealarm.domain.repository

import com.example.firealarm.presentation.utils.NetworkState
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(username: String, password: String): Flow<NetworkState>
}

