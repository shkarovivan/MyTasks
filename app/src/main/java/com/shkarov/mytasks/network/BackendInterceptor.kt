package com.shkarov.mytasks.network

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response

// Points every request at the user-configured backend URL and adds the
// shared X-Api-Key secret. The provider key never exists in the app.
class BackendInterceptor(
    defaultUrl: String
) : Interceptor {

    @Volatile
    var baseUrl: String = defaultUrl

    @Volatile
    var apiKey: String = ""

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val baseHttpUrl = baseUrl.toHttpUrl()

        val newUrl = original.url.newBuilder()
            .scheme(baseHttpUrl.scheme)
            .host(baseHttpUrl.host)
            .port(baseHttpUrl.port)
            .build()

        val newRequest = original.newBuilder()
            .url(newUrl)
            .addHeader("X-Api-Key", apiKey)
            .build()

        return chain.proceed(newRequest)
    }
}
