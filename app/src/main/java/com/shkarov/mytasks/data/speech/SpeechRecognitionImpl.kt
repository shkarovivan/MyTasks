package com.shkarov.mytasks.data.speech

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import timber.log.Timber
import android.Manifest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SpeechRecognitionImpl(
    private val context: Context
) : SpeechRecognition {

    private val _recognitionState = MutableStateFlow<SpeechRecognitionState>(SpeechRecognitionState.Stopped)

    override val recognitionState = _recognitionState.asStateFlow()
    private var speechRecognizer: SpeechRecognizer? = null

    override fun startRecognize() {
        Timber.d("$TAG startRecognize called")

        if (!hasAudioPermission()) {
            Timber.w("$TAG Audio permission not granted")
            _recognitionState.tryEmit(SpeechRecognitionState.PermissionDenied)
            return
        }

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Timber.w("$TAG Speech recognition not available")
            _recognitionState.tryEmit(SpeechRecognitionState.Error("Speech recognition not available"))
            return
        }

        try {
            initializeSpeechRecognizer()
            startListening()
        } catch (e: Exception) {
            Timber.e(e, "$TAG Error starting speech recognition")
            _recognitionState.tryEmit(SpeechRecognitionState.Error("Failed to start recognition: ${e.message}"))
        }
    }

    private fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        Timber.d("$TAG initializeSpeechRecognizer")
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Timber.d("$TAG onReadyForSpeech")
                _recognitionState.tryEmit(SpeechRecognitionState.Ready)
            }

            override fun onBeginningOfSpeech() {
                Timber.d("$TAG onBeginningOfSpeech")
                _recognitionState.tryEmit(SpeechRecognitionState.Listening)
            }

            override fun onResults(results: Bundle?) {
                val data = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                Timber.d("$TAG onResults - $results")
                if (data != null && data.isNotEmpty()) {
                    _recognitionState.tryEmit(SpeechRecognitionState.FinalResult(data[0]))
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val recognitionData = extractRecognitionData(partialResults)
                recognitionData?.let {
                    if (it.results.isNotEmpty()) {
                        Timber.d("$TAG onPartialResults data - ${it.results.first()}")
                        val result = it.results.first()
                        _recognitionState.tryEmit(
                            if (recognitionData.isFinal) {
                                SpeechRecognitionState.FinalResult(result)
                            } else {
                                SpeechRecognitionState.PartialResult(result)
                            }
                        )
                    }
                }
            }

            override fun onError(error: Int) {
                Timber.d("$TAG onError - $error")
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
        Timber.d("$TAG startListening")

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ru-RU")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000)
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                3000
            )
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 2000)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Скажите что-нибудь на русском языке...")


        }

        try {
            speechRecognizer?.startListening(intent)
            _recognitionState.tryEmit(SpeechRecognitionState.Initializing)
        } catch (e: Exception) {
            Timber.e(e, "$TAG Error starting listener")
            _recognitionState.tryEmit(SpeechRecognitionState.Error("Failed to start listening: ${e.message}"))
        }
    }

    override fun stopListening() {
        speechRecognizer?.stopListening()
    }

    override fun stopRecognition() {
        Timber.d("$TAG stopRecognition")
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
            _recognitionState.tryEmit(SpeechRecognitionState.Stopped)
        } catch (e: Exception) {
            Timber.e(e, "$TAG Error stopping recognition")
        }
    }

    override fun checkPermission(): Boolean {
        return hasAudioPermission()
    }

    private fun getErrorMessage(error: Int): String {
        return when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input detected"
            else -> "Unknown error (code: $error)"
        }
    }

    private fun extractRecognitionData(bundle: Bundle?): RecognitionData? {
        if (bundle == null) return null

        return try {
            val results = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val confidenceScores = bundle.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
            val isFinal = bundle.getBoolean("final_result", false)
            val locale = bundle.getString("current_locale", "")

            RecognitionData(
                results = results ?: emptyList(),
                confidenceScores = confidenceScores?.toList() ?: emptyList(),
                isFinal = isFinal,
                locale = locale
            )
        } catch (e: Exception) {
            Timber.e(e, "$TAG Ошибка извлечения данных из Bundle: ${e.message}")
            null
        }
    }

    companion object {
        private const val TAG = "SpeechRecognitionImpl"
    }
}