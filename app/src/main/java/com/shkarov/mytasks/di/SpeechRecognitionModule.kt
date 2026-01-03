package com.shkarov.mytasks.di

import android.content.Context
import com.shkarov.mytasks.data.speech.SpeechRecognition
import com.shkarov.mytasks.data.speech.SpeechRecognitionImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class SpeechRecognitionModule {

    @Singleton
    @Provides
    fun provideSpeechRecognition(@ApplicationContext context: Context): SpeechRecognition {
        return SpeechRecognitionImpl(context = context).also {
            Timber.d("✅ SpeechRecognitionModule: SpeechRecognition создан: $it")
        }
    }
}