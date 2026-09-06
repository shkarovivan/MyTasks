package com.shkarov.mytasks.network

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    const val USE_UNSAFE = false

    private const val CONNECT_TIMEOUT_SECONDS = 30L
    private const val READ_TIMEOUT_SECONDS = 120L
    private const val WRITE_TIMEOUT_SECONDS = 120L

    private fun baseClientBuilder(): OkHttpClient.Builder =
        OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)

    // Keys must never appear in logcat; BASIC level keeps bodies out as well.
    private fun addLogging(builder: OkHttpClient.Builder) {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
            redactHeader("Authorization")
            redactHeader("X-Api-Key")
        }
        builder.addInterceptor(loggingInterceptor)
    }

    fun create(
        aiProviderInterceptor: AiProviderInterceptor,
        baseUrl: String,
        enableLogging: Boolean = true
    ): ApiService {
        val clientBuilder = baseClientBuilder()
            .addInterceptor(aiProviderInterceptor)

        if (enableLogging) {
            addLogging(clientBuilder)
        }

        // If HTTPS, add the unsafe client (development only!)
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

    fun createBackend(
        backendInterceptor: BackendInterceptor,
        baseUrl: String,
        enableLogging: Boolean = true
    ): BackendApi {
        val clientBuilder = baseClientBuilder()
            .addInterceptor(backendInterceptor)

        if (enableLogging) {
            addLogging(clientBuilder)
        }

        // Same Gson settings as the direct mode task JSON, so the backend
        // receives task bytes identical to what direct prompts embed.
        val gson = GsonBuilder()
            .disableHtmlEscaping()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(ensureTrailingSlash(baseUrl))
            .client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit.create(BackendApi::class.java)
    }

    fun createSync(
        authInterceptor: AuthInterceptor,
        baseUrl: String,
        enableLogging: Boolean = true
    ): SyncApi {
        val clientBuilder = baseClientBuilder()
            .addInterceptor(authInterceptor)

        if (enableLogging) {
            addLogging(clientBuilder)
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(ensureTrailingSlash(baseUrl))
            .client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(SyncApi::class.java)
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
