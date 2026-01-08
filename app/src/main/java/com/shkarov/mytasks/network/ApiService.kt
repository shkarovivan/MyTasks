package com.shkarov.mytasks.network

import com.shkarov.mytasks.network.data.ApiResponse
import com.shkarov.mytasks.network.data.ChatRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {

    @POST("openai/v1/chat/completions")
    @Headers("Content-Type: application/json")
    suspend fun chatRequest(@Body request: ChatRequest): Response<ApiResponse>
}