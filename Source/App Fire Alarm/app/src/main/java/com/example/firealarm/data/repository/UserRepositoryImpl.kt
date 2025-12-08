package com.example.firealarm.data.repository

import android.util.Log
import com.example.firealarm.data.api.ApiService
import com.example.firealarm.data.model.user.CreateUserRequest
import com.example.firealarm.data.model.user.UpdateUserRequest
import com.example.firealarm.data.model.user.UserDto
import com.example.firealarm.data.model.user.UserResponse
import com.example.firealarm.data.model.user.toDomain
import com.example.firealarm.domain.model.UserInfo
import com.example.firealarm.domain.repository.UserRepository
import com.example.firealarm.presentation.utils.Constant
import com.example.firealarm.presentation.utils.NetworkState
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val gson: Gson
) : UserRepository {
    private val TAG = "UserRepository"

    override suspend fun getUsers(): Flow<NetworkState> = callbackFlow {
        trySend(NetworkState.Loading)
        try {
            Log.d(TAG, "Fetching users")
            val response = apiService.getUsers()

            if (response.code == 200 && response.result != null) {
                val deviceResponse = apiService.getAllDevices()
                var listUser = response.result.content
                    .filter { it.role != Constant.admin }
                    .sortedBy { it.id }
                val usersWithDeviceCount = listUser.map { userDto ->
                    val deviceCount = deviceResponse.result?.content?.filter { deviceDto -> deviceDto.userId == userDto.id }?.size ?: 0
                    Log.d(TAG, "Count device user ${userDto.username}: $deviceCount")
                    UserInfo(
                        id = userDto.id,
                        username = userDto.username,
                        deviceCount = deviceCount,
                        phone = userDto.phoneNumber ?: "",
                        role = userDto.role
                    )
                }
                Log.d(TAG, "Successfully fetched ${usersWithDeviceCount.size} users")
                trySend(NetworkState.Success(usersWithDeviceCount))
            } else {
                Log.e(TAG, "Failed to fetch users. Code: ${response.code}, Message: ${response.message}")
                trySend(NetworkState.Error("Failed to fetch users: ${response.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching users: ${e.message}", e)
            trySend(NetworkState.Error(e.message ?: "Lỗi khi lấy danh sách người dùng"))
        }
        awaitClose {  }
    }
    
    override suspend fun createUser(username: String, password: String, phone: String): Flow<NetworkState> = callbackFlow {
        trySend(NetworkState.Loading)
        try {
            Log.d(TAG, "Creating user: $username")
            val request = CreateUserRequest(
                username = username,
                password = password,
                phoneNumber = phone,
                role = Constant.user
            )
            val response = apiService.createUser(request)
            
            if (response.code == 200) {
                Log.d(TAG, "Successfully created user")
                trySend(NetworkState.Success(Unit))
            } else {
                Log.e(TAG, "Failed to create user. Code: ${response.code}, Message: ${response.message}")
                trySend(NetworkState.Error("Failed to create user: ${response.message}"))
            }
        } catch (e: HttpException) {
            // Xử lý HTTP error (400, 500, etc.) - lấy message từ error body
            val errorMessage = try {
                val errorBody = e.response()?.errorBody()?.string()
                if (!errorBody.isNullOrEmpty()) {
                    val errorResponse = gson.fromJson(errorBody, UserResponse::class.java)
                    errorResponse.message ?: "Lỗi khi tạo người dùng"
                } else {
                    "Lỗi khi tạo người dùng: HTTP ${e.code()}"
                }
            } catch (ex: Exception) {
                Log.e(TAG, "Error reading error body: ${ex.message}", ex)
                "Lỗi khi tạo người dùng: HTTP ${e.code()}"
            }
            trySend(NetworkState.Error(errorMessage))
        } catch (e: IOException) {
            Log.e(TAG, "Network error creating user: ${e.message}", e)
            trySend(NetworkState.Error("Lỗi kết nối mạng. Vui lòng kiểm tra kết nối internet."))
        } catch (e: Exception) {
            Log.e(TAG, "Error creating user: ${e.message}", e)
            trySend(NetworkState.Error(e.message ?: "Lỗi khi tạo người dùng"))
        }
        awaitClose { }
    }
    
    override suspend fun updateUser(phoneNumber: String): Flow<NetworkState> = callbackFlow {
        trySend(NetworkState.Loading)
        try {
            Log.d(TAG, "Updating user")
            val request = UpdateUserRequest(phoneNumber = phoneNumber)
            val response = apiService.updateUser(request)
            
            if (response.code == 200) {
                Log.d(TAG, "Successfully updated user")
                trySend(NetworkState.Success(Unit))
            } else {
                Log.e(TAG, "Failed to update user. Code: ${response.code}, Message: ${response.message}")
                trySend(NetworkState.Error("Failed to update user: ${response.message}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user: ${e.message}", e)
            trySend(NetworkState.Error(e.message ?: "Lỗi khi cập nhật người dùng"))
        }
        awaitClose { }
    }
    
    override suspend fun deleteUser(userId: Int): Flow<NetworkState> = callbackFlow {
        trySend(NetworkState.Loading)
        try {
            Log.d(TAG, "Deleting user: $userId")
            val response = apiService.deleteUser(userId)
            
            if (response.code == 200) {
                Log.d(TAG, "Successfully deleted user")
                trySend(NetworkState.Success(Unit))
            } else {
                Log.e(TAG, "Failed to delete user. Code: ${response.code}, Message: ${response.message}")
                trySend(NetworkState.Error("Failed to delete user: ${response.message}"))
            }
        }
        catch (e: Exception) {
            Log.e(TAG, "Error deleting user: ${e.message}", e)
            trySend(NetworkState.Error(e.message ?: "Lỗi khi xóa người dùng"))
        }
        awaitClose {  }
    }

    override suspend fun getUserInfor(): Flow<NetworkState> = callbackFlow{
        trySend(NetworkState.Loading)
        try {
            Log.d(TAG, "Get user information")
            val response = apiService.getUserInformation()

            if (response.code == 200) {
                Log.d(TAG, "Successfully fetch user")
                trySend(NetworkState.Success(response.result.toDomain()))
            } else {
                Log.e(TAG, "Failed to fetch user. Code: ${response.code}, Message: ${response.message}")
                trySend(NetworkState.Error("Failed to fetch user: ${response.message}"))
            }
        }
        catch (e: Exception) {
            Log.e(TAG, "Error fetch user: ${e.message}", e)
            trySend(NetworkState.Error(e.message ?: "Lỗi khi lấy thông tin người dùng"))
        }
        awaitClose {  }
    }
}

