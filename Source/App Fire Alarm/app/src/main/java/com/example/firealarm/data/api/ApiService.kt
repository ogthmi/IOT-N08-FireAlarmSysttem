package com.example.firealarm.data.api

import com.example.firealarm.data.model.ControlRequest
import com.example.firealarm.data.model.ControlResponse
import com.example.firealarm.data.model.device.DeviceResponse
import com.example.firealarm.data.model.auth.LoginRequest
import com.example.firealarm.data.model.auth.LoginResponse
import com.example.firealarm.data.model.threshold.ThresholdResponse
import com.example.firealarm.data.model.threshold.ThresholdDto
import com.example.firealarm.data.model.user.UserResponse
import com.example.firealarm.data.model.user.CreateUserRequest
import com.example.firealarm.data.model.user.UpdateUserRequest
import com.example.firealarm.data.model.device.CreateDeviceRequest
import com.example.firealarm.data.model.device.UpdateDeviceRequest
import com.example.firealarm.data.model.user.UserInforResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface ApiService {

    // Đăng nhập
    @POST("api/v1/users/log-in")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse


    // Devices
    // Lấy danh sách thiết bị (User)
    @GET("api/v1/devices/user")
    suspend fun getDeviceByUser(): DeviceResponse

    // Lấy danh sách tất cả thiết bị
    @GET("api/v1/devices")
    suspend fun getAllDevices(): DeviceResponse

    // Tạo thiết bị mới
    @POST("api/v1/devices")
    suspend fun createDevice(
        @Body request: CreateDeviceRequest
    ): DeviceResponse


    // Cập nhật thông tin thiết bị
    @PUT("api/v1/devices")
    suspend fun updateDevice(
        @Query("deviceId") deviceId: String,
        @Body request: UpdateDeviceRequest
    ): DeviceResponse
    
    // Xóa thiết bị
    @DELETE("api/v1/devices")
    suspend fun deleteDevice(
        @Query("deviceId") deviceId: String
    ): DeviceResponse

    @POST("api/mqtt/send-topic")
    suspend fun sendBuzzerControl(
        @Query("topic") topic: String,
        @Body request: ControlRequest
    ): ControlResponse
    
    @POST("api/mqtt/send-topic")
    suspend fun sendPumpControl(
        @Query("topic") topic: String,
        @Body request: ControlRequest
    ): ControlResponse


    // Threshold
    // Lấy ngưỡng
    @GET("api/v1/rules/threshold")
    suspend fun getThreshold(
        @Query("deviceId") deviceId: String
    ): ThresholdResponse
    
    // Thay đổi ngưỡng
    @POST("api/v1/rules/threshold")
    suspend fun updateThreshold(
        @Query("deviceId") deviceId: String,
        @Body request: List<ThresholdDto>
    ): ThresholdResponse


    // User
    // Lấy danh sách người dùng (Admin)
    @GET("api/v1/users/all")
    suspend fun getUsers(): UserResponse
    
    // Tạo người dùng mới (Admin)
    @POST("api/v1/users/sign-up")
    suspend fun createUser(
        @Body request: CreateUserRequest
    ): UserResponse

    // Lấy thông tin người dùng cụ thể
    @GET("api/v1/users")
    suspend fun getUserInformation(): UserInforResponse

    // Xóa người dùng (Admin)
    @DELETE("api/v1/users")
    suspend fun deleteUser(
        @Query("userId") userId: Int
    ): UserResponse
    
    // Cập nhật thông tin người dùng (Admin)
    @PUT("api/v1/users")
    suspend fun updateUser(
        @Body request: UpdateUserRequest
    ): UserResponse
}

