package com.example.firealarm.domain.repository

import com.example.firealarm.domain.model.UserInfo
import com.example.firealarm.presentation.utils.NetworkState
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getUsers(): Flow<NetworkState>
    suspend fun createUser(username: String, password: String, phone: String): Flow<NetworkState>
    suspend fun updateUser(phoneNumber: String): Flow<NetworkState>
    suspend fun deleteUser(userId: Int): Flow<NetworkState>
    suspend fun getUserInfor(): Flow<NetworkState>
}

