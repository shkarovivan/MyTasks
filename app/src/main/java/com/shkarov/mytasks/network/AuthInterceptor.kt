package com.shkarov.mytasks.network

import com.shkarov.mytasks.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response

// Points sync/auth requests at the user-configured backend URL and adds
// Authorization: Bearer <Google ID-token>. Retries once with a fresh token
// on 401 (tokens live ~1h and may expire between requests).
class AuthInterceptor(
    private val authRepository: AuthRepository,
    defaultUrl: String
) : Interceptor {

    @Volatile
    var baseUrl: String = defaultUrl

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val baseHttpUrl = baseUrl.toHttpUrl()

        val url = original.url.newBuilder()
            .scheme(baseHttpUrl.scheme)
            .host(baseHttpUrl.host)
            .port(baseHttpUrl.port)
            .build()

        var token = fetchToken()
        val request = original.newBuilder()
            .url(url)
            .apply { if (token != null) header("Authorization", "Bearer $token") }
            .build()

        var response = chain.proceed(request)
        if (response.code == 401 && token != null) {
            response.close()
            authRepository.invalidateToken()
            token = fetchToken()
            if (token != null) {
                val retry = original.newBuilder()
                    .url(url)
                    .header("Authorization", "Bearer $token")
                    .build()
                response = chain.proceed(retry)
            }
        }
        return response
    }

    // OkHttp interceptors are blocking; the silent credential flow is
    // non-interactive, so a bounded blocking bridge is safe here.
    private fun fetchToken(): String? =
        runBlocking { runCatching { authRepository.getIdToken() }.getOrNull() }
}
