package com.shkarov.mytasks.di

import com.shkarov.mytasks.BuildConfig
import com.shkarov.mytasks.network.ApiService
import com.shkarov.mytasks.network.AiProviderInterceptor
import com.shkarov.mytasks.network.AuthInterceptor
import com.shkarov.mytasks.network.BackendApi
import com.shkarov.mytasks.network.BackendInterceptor
import com.shkarov.mytasks.network.RetrofitClient
import com.shkarov.mytasks.network.SyncApi
import com.shkarov.mytasks.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    private const val DEFAULT_URL: String = "https://api.proxyapi.ru"

    @Provides
    @Singleton
    fun provideDynamicUrlInterceptor(): AiProviderInterceptor {
        return AiProviderInterceptor(DEFAULT_URL)
    }

    @Provides
    @Singleton
    fun provideApiService(aiProviderInterceptor: AiProviderInterceptor): ApiService {
        return RetrofitClient.create(
            aiProviderInterceptor = aiProviderInterceptor,
            baseUrl = DEFAULT_URL)
    }

    @Provides
    @Singleton
    fun provideBackendInterceptor(): BackendInterceptor {
        return BackendInterceptor(BuildConfig.BACKEND_URL)
    }

    @Provides
    @Singleton
    fun provideBackendApi(backendInterceptor: BackendInterceptor): BackendApi {
        return RetrofitClient.createBackend(
            backendInterceptor = backendInterceptor,
            baseUrl = BuildConfig.BACKEND_URL)
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(authRepository: AuthRepository): AuthInterceptor {
        return AuthInterceptor(authRepository, BuildConfig.BACKEND_URL)
    }

    @Provides
    @Singleton
    fun provideSyncApi(authInterceptor: AuthInterceptor): SyncApi {
        return RetrofitClient.createSync(
            authInterceptor = authInterceptor,
            baseUrl = BuildConfig.BACKEND_URL)
    }
}
