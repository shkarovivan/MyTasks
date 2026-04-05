package com.shkarov.mytasks.network

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response

class DynamicUrlInterceptor(
    defaultUrl: String
) : Interceptor {

    @Volatile
    var baseUrl: String = defaultUrl

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val newUrl = original.url.newBuilder()
            .scheme(baseUrl.toHttpUrl().scheme)
            .host(baseUrl.toHttpUrl().host)
            .port(baseUrl.toHttpUrl().port)
            .build()

        val newRequest = original.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}