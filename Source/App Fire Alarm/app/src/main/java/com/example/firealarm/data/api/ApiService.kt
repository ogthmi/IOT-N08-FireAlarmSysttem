package com.example.firealarm.data.api

import com.example.firealarm.data.model.ControlRequest
import com.example.firealarm.data.model.ControlResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
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
}

