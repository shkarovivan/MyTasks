package com.shkarov.mytasks.network

import com.shkarov.mytasks.network.data.BackendSearchRequest
import com.shkarov.mytasks.network.data.BackendSearchResponse
import com.shkarov.mytasks.network.data.BackendTaskRequest
import com.shkarov.mytasks.network.data.TaskResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

// API of the MyTasksBackend server: it holds the provider key,
// builds prompts and returns the final structured JSON.
interface BackendApi {

    @POST("v1/task")
    @Headers("Content-Type: application/json")
    suspend fun createTask(@Body request: BackendTaskRequest): Response<TaskResponse>

    @POST("v1/search")
    @Headers("Content-Type: application/json")
    suspend fun search(@Body request: BackendSearchRequest): Response<BackendSearchResponse>
}
