package com.shkarov.mytasks.di

import com.shkarov.mytasks.data.speech.SpeechRecognition
import com.shkarov.mytasks.data.speech.SpeechRecognitionImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class SpeechRecognitionModule {

    @Singleton
    @Provides
    fun provideSpeechRecognition(speechRecognition: SpeechRecognitionImpl): SpeechRecognition {
        speechRecognition
        Timber.d("✅ SpeechRecognitionModule: SpeechRecognition создан: $speechRecognition")
        return  speechRecognition
    }
}