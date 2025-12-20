package com.shkarov.mytasks.data.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class SpeechRecognitionImpl(
    private val context: Context
) : SpeechRecognition{

    private val _recognitionState = MutableSharedFlow<SpeechRecognitionState>()
    private var speechRecognizer: SpeechRecognizer? = null

    override suspend fun startRecognize(): Flow<SpeechRecognitionState> {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _recognitionState.emit(SpeechRecognitionState.Error("Speech recognition not available"))
            return _recognitionState.asSharedFlow()
        }

        initializeSpeechRecognizer()
        startListening()

        return _recognitionState.asSharedFlow()
    }

    private fun initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _recognitionState.tryEmit(SpeechRecognitionState.Ready)
            }

            override fun onBeginningOfSpeech() {
                _recognitionState.tryEmit(SpeechRecognitionState.Listening)
            }

            override fun onResults(results: Bundle?) {
                val data = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (data != null && data.isNotEmpty()) {
                    _recognitionState.tryEmit(SpeechRecognitionState.FinalResult(data[0]))
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val data = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (data != null && data.isNotEmpty()) {
                    _recognitionState.tryEmit(SpeechRecognitionState.PartialResult(data[0]))
                }
            }

            override fun onError(error: Int) {
                _recognitionState.tryEmit(SpeechRecognitionState.Error(getErrorMessage(error)))
            }


            override fun onBufferReceived(buffer: ByteArray?) {
                //TODO("Not yet implemented")
            }

            override fun onEndOfSpeech() {
                //TODO("Not yet implemented")
            }

            override fun onRmsChanged(rmsdB: Float) {
                //TODO("Not yet implemented")
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                //TODO("Not yet implemented")
            }

        })
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            // ... настройки как в предыдущем примере
        }
        speechRecognizer?.startListening(intent)
    }

    override fun stopRecognition() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
    }

    private fun getErrorMessage(error: Int): String {
        // ... реализация как выше
        return "Error: $error"
    }
}