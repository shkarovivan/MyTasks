package com.shkarov.mytasks.network

import com.shkarov.mytasks.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {

    private val token = getAccessToken()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }

    private fun getAccessToken(): String {
        return BuildConfig.API_TOKEN
    }
}