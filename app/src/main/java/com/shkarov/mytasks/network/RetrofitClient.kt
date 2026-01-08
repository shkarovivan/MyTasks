package com.shkarov.mytasks.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    const val USE_UNSAFE = false

    fun create(baseUrl: String, enableLogging: Boolean = true): ApiService {
        val clientBuilder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor())

        // Добавляем логирование
        if (enableLogging) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            clientBuilder.addInterceptor(loggingInterceptor)
        }

        // Если HTTPS, добавляем unsafe client (только для разработки!)
        if (USE_UNSAFE && baseUrl.startsWith("https://", ignoreCase = true)) {
            makeUnsafe(clientBuilder)
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(ensureTrailingSlash(baseUrl))
            .client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(ApiService::class.java)
    }

    private fun makeUnsafe(builder: OkHttpClient.Builder) {
        try {
            val trustAllCerts = arrayOf<javax.net.ssl.TrustManager>(
                object : javax.net.ssl.X509TrustManager {
                    override fun checkClientTrusted(
                        chain: Array<java.security.cert.X509Certificate>,
                        authType: String
                    ) {}

                    override fun checkServerTrusted(
                        chain: Array<java.security.cert.X509Certificate>,
                        authType: String
                    ) {}

                    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                        return arrayOf()
                    }
                }
            )

            val sslContext = javax.net.ssl.SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())

            val sslSocketFactory = sslContext.socketFactory

            builder.sslSocketFactory(
                sslSocketFactory,
                trustAllCerts[0] as javax.net.ssl.X509TrustManager
            )
            builder.hostnameVerifier { _, _ -> true }
        } catch (e: Exception) {
            throw RuntimeException("Failed to create unsafe HTTPS client", e)
        }
    }

    private fun ensureTrailingSlash(url: String): String {
        return if (url.endsWith("/")) url else "$url/"
    }
}
