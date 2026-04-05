package com.shkarov.mytasks.di

import com.shkarov.mytasks.network.ApiService
import com.shkarov.mytasks.network.DynamicUrlInterceptor
import com.shkarov.mytasks.network.RetrofitClient
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
    fun provideDynamicUrlInterceptor(): DynamicUrlInterceptor {
        return DynamicUrlInterceptor(DEFAULT_URL)
    }

    @Provides
    @Singleton
    fun provideApiService(dynamicUrlInterceptor: DynamicUrlInterceptor): ApiService {
        return RetrofitClient.create(
            dynamicUrlInterceptor = dynamicUrlInterceptor,
            baseUrl = DEFAULT_URL)
    }
}