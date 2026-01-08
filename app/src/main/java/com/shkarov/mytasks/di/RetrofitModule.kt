package com.shkarov.mytasks.di

import com.shkarov.mytasks.network.ApiService
import com.shkarov.mytasks.network.RetrofitClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    @Provides
    @Singleton
    fun provideApiService(): ApiService{
        return RetrofitClient.create("https://api.proxyapi.ru")
    }
}