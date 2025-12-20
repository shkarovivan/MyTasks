package com.shkarov.mytasks.data.speech

import kotlinx.coroutines.flow.Flow

interface SpeechRecognition {
    suspend fun startRecognize(): Flow<SpeechRecognitionState>
    fun stopRecognition()
}

sealed class SpeechRecognitionState {
    object Ready : SpeechRecognitionState()
    object Listening : SpeechRecognitionState()
    data class PartialResult(val text: String) : SpeechRecognitionState()
    data class FinalResult(val text: String) : SpeechRecognitionState()
    data class Error(val message: String) : SpeechRecognitionState()
}