package com.shkarov.mytasks.data.speech

import kotlinx.coroutines.flow.StateFlow

interface SpeechRecognition {
    fun startRecognize()

    val recognitionState: StateFlow<SpeechRecognitionState>

    fun stopRecognition()

    fun checkPermission(): Boolean
}

sealed class SpeechRecognitionState {
    object Initializing : SpeechRecognitionState()
    object Ready : SpeechRecognitionState()
    object Listening : SpeechRecognitionState()
    data class PartialResult(val text: String) : SpeechRecognitionState()
    data class FinalResult(val text: String) : SpeechRecognitionState()
    data class Error(val message: String) : SpeechRecognitionState()
    object SpeechEnded : SpeechRecognitionState()
    object Stopped : SpeechRecognitionState()
    object PermissionDenied : SpeechRecognitionState()

}