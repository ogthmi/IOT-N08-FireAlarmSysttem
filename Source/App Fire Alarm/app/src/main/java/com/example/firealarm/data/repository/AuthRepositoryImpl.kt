package com.example.firealarm.data.repository

import android.util.Log
import com.example.firealarm.data.api.ApiService
import com.example.firealarm.data.model.auth.LoginRequest
import com.example.firealarm.data.model.auth.toDomain
import com.example.firealarm.domain.repository.AuthRepository
import com.example.firealarm.presentation.utils.NetworkState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : AuthRepository {
    private val TAG = "AuthRepository"
    
    override suspend fun login(username: String, password: String): Flow<NetworkState> = callbackFlow {
        trySend(NetworkState.Loading)
        try {
            val request = LoginRequest(
                username = username,
                password = password
            )
            Log.d(TAG, "Sending login request for username: $username")
            val response = apiService.login(request)

            if (response.code == 200 && response.result != null) {
                Log.d(TAG, "Login successful:Access Token - ${response.result.accessToken}")
                val user = response.toDomain()
                trySend(NetworkState.Success(user))
            } else {
                Log.e(TAG, "Login failed. Code: ${response.code}, Message: ${response.message}")
                trySend(NetworkState.Error("Login failed: ${response.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during login: ${e.message}", e)
            trySend(NetworkState.Error(e.message ?: "Đăng nhập thất bại"))
        }
        awaitClose {  }
    }
}

