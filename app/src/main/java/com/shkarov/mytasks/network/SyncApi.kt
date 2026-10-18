package com.shkarov.mytasks.network

import com.google.gson.annotations.SerializedName
import com.shkarov.mytasks.network.data.SyncPullResponse
import com.shkarov.mytasks.network.data.SyncPushRequest
import com.shkarov.mytasks.network.data.SyncPushResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class WhoamiResponse(
    @SerializedName("user_id") val userId: String,
    val email: String
)

// Authenticated API of the MyTasksBackend server (Bearer = Google ID-token).
interface SyncApi {

    @GET("v1/auth/whoami")
    suspend fun whoami(): Response<WhoamiResponse>

    @POST("v1/sync/push")
    suspend fun push(@Body request: SyncPushRequest): Response<SyncPushResponse>

    @GET("v1/sync/pull")
    suspend fun pull(
        @Query("since") since: Long,
        @Query("limit") limit: Int = 500
    ): Response<SyncPullResponse>
}
