package com.shkarov.mytasks.network

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

class AiProviderInterceptor(
    defaultUrl: String
) : Interceptor {

    @Volatile
    var baseUrl: String = defaultUrl

    @Volatile
    var providerToken: String = ""

    override fun intercept(chain: Interceptor.Chain): Response {
        Timber.d("providerKeyInterceptor- $providerToken")
        val original = chain.request()
        val baseHttpUrl = baseUrl.toHttpUrl()

        val newUrl = original.url.newBuilder()
            .scheme(baseHttpUrl.scheme)
            .host(baseHttpUrl.host)
            .port(baseHttpUrl.port)
            .build()

        val newRequest = original.newBuilder()
            .url(newUrl)
            .addHeader("Authorization", "Bearer $providerToken")
            .build()

        return chain.proceed(newRequest)
    }
}