package com.shkarov.mytasks.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.GET

data class WhoamiResponse(
    @SerializedName("user_id") val userId: String,
    val email: String
)

// Authenticated API of the MyTasksBackend server (Bearer = Google ID-token).
// Sync endpoints will be added here in the next migration stage.
interface SyncApi {

    @GET("v1/auth/whoami")
    suspend fun whoami(): Response<WhoamiResponse>
}
