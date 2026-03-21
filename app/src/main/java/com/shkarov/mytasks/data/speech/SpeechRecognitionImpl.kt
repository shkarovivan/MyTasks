package com.shkarov.mytasks.data.speech

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import com.shkarov.mytasks.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

class SpeechRecognitionImpl(
    private val context: Context
) : SpeechRecognition {

    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _recognitionState =
        MutableStateFlow<SpeechRecognitionState>(SpeechRecognitionState.Stopped)

    override val recognitionState = _recognitionState.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var isRestarting = false
    private var isReleasing = false

    override fun startRecognize() {
        runOnMain {
            Timber.d("$TAG startRecognize called")

            if (!hasAudioPermission()) {
                Timber.w("$TAG Audio permission not granted")
                _recognitionState.tryEmit(SpeechRecognitionState.PermissionDenied)
                return@runOnMain
            }

            if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
                Timber.w("$TAG Speech recognition not available")
                _recognitionState.tryEmit(
                    SpeechRecognitionState.Error("Speech recognition not available")
                )
                return@runOnMain
            }

            try {
                initializeSpeechRecognizerIfNeeded()
                restartListening()
            } catch (e: Exception) {
                Timber.e(e, "$TAG Error starting speech recognition")
                _recognitionState.tryEmit(
                    SpeechRecognitionState.Error("Failed to start recognition: ${e.message}")
                )
            }
        }
    }

    private fun initializeSpeechRecognizerIfNeeded() {
        if (speechRecognizer != null) return

        Timber.d("$TAG initializeSpeechRecognizerIfNeeded")

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(appContext).apply {
            setRecognitionListener(recognitionListener)
        }
    }

    private val recognitionListener = object : RecognitionListener {

        override fun onReadyForSpeech(params: Bundle?) {
            if (isReleasing) return
            Timber.d("$TAG onReadyForSpeech")
            _recognitionState.tryEmit(SpeechRecognitionState.Ready)
        }

        override fun onBeginningOfSpeech() {
            if (isReleasing) return
            Timber.d("$TAG onBeginningOfSpeech")
            _recognitionState.tryEmit(SpeechRecognitionState.Listening)
        }

        override fun onResults(results: Bundle?) {
            if (isReleasing) return

            val data = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            Timber.d("$TAG onResults - $data")

            if (!data.isNullOrEmpty()) {
                _recognitionState.tryEmit(SpeechRecognitionState.FinalResult(data.first()))
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            if (isReleasing) return

            val recognitionData = extractRecognitionData(partialResults)
            recognitionData?.let {
                if (it.results.isNotEmpty()) {
                    val result = it.results.first()
                    Timber.d("$TAG onPartialResults data - $result")
                    _recognitionState.tryEmit(
                        if (it.isFinal) {
                            SpeechRecognitionState.FinalResult(result)
                        } else {
                            SpeechRecognitionState.PartialResult(result)
                        }
                    )
                }
            }
        }

        override fun onError(error: Int) {
            if (isReleasing) {
                Timber.d("$TAG ignore onError=$error during release")
                return
            }

            if (isRestarting && error == SpeechRecognizer.ERROR_CLIENT) {
                Timber.d("$TAG ignore ERROR_CLIENT during restart")
                return
            }

            Timber.d("$TAG onError - $error (${getErrorMessage(error)})")

            when (error) {
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                    _recognitionState.tryEmit(
                        SpeechRecognitionState.Error("No speech input detected")
                    )
                }

                SpeechRecognizer.ERROR_SERVER_DISCONNECTED -> {
                    // Не показываем это как user-facing ошибку
                    // Просто пересоздадим recognizer на следующем старте
                    releaseSpeechRecognizer()
                    _recognitionState.tryEmit(SpeechRecognitionState.Stopped)
                }

                else -> {
                    _recognitionState.tryEmit(
                        SpeechRecognitionState.Error(getErrorMessage(error))
                    )
                }
            }
        }

        override fun onBufferReceived(buffer: ByteArray?) = Unit

        override fun onEndOfSpeech() {
            if (isReleasing) return
            Timber.d("$TAG onEndOfSpeech")
        }

        override fun onRmsChanged(rmsdB: Float) = Unit

        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }

    private fun restartListening() {
        Timber.d("$TAG restartListening")

        val recognizer = speechRecognizer
        if (recognizer == null) {
            _recognitionState.tryEmit(
                SpeechRecognitionState.Error("SpeechRecognizer is not initialized")
            )
            return
        }

        isRestarting = true

        try {
            recognizer.cancel()
        } catch (e: Exception) {
            Timber.e(e, "$TAG Error canceling before restart")
        }

        mainHandler.postDelayed({
            isRestarting = false

            try {
                speechRecognizer?.startListening(createRecognitionIntent())
                _recognitionState.tryEmit(SpeechRecognitionState.Initializing)
            } catch (e: Exception) {
                Timber.e(e, "$TAG Error starting listener")
                _recognitionState.tryEmit(
                    SpeechRecognitionState.Error("Failed to start listening: ${e.message}")
                )
            }
        }, RESTART_DELAY_MS)
    }

    override fun stopListening() {
        runOnMain {
            try {
                Timber.d("$TAG stopListening")
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Timber.e(e, "$TAG Error stopping listening")
            }
        }
    }

    override fun stopRecognition() {
        runOnMain {
            Timber.d("$TAG stopRecognition")
            releaseSpeechRecognizer()
            _recognitionState.tryEmit(SpeechRecognitionState.Stopped)
        }
    }

    private fun releaseSpeechRecognizer() {
        val recognizer = speechRecognizer ?: return

        Timber.d("$TAG releaseSpeechRecognizer")
        isReleasing = true

        try {
            recognizer.cancel()
        } catch (e: Exception) {
            Timber.e(e, "$TAG Error canceling recognizer")
        }

        try {
            recognizer.destroy()
        } catch (e: Exception) {
            Timber.e(e, "$TAG Error destroying recognizer")
        } finally {
            speechRecognizer = null
            isReleasing = false
        }
    }

    override fun checkPermission(): Boolean = hasAudioPermission()

    private fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun createRecognitionIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ru-RU")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, appContext.packageName)

            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                7000L
            )
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                7000L
            )
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,
                10000L
            )

            putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                context.getString(R.string.speech_recognition_prompt)
            )
        }
    }

    private fun runOnMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            mainHandler.post(block)
        }
    }

    private fun getErrorMessage(error: Int): String {
        return when (error) {
            SpeechRecognizer.ERROR_AUDIO -> context.getString(R.string.speech_error_audio)
            SpeechRecognizer.ERROR_CLIENT -> context.getString(R.string.speech_error_client)
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                context.getString(R.string.speech_error_insufficient_permissions)
            SpeechRecognizer.ERROR_NETWORK -> context.getString(R.string.speech_error_network)
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                context.getString(R.string.speech_error_network_timeout)
            SpeechRecognizer.ERROR_NO_MATCH -> context.getString(R.string.speech_error_no_match)
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                context.getString(R.string.speech_error_recognizer_busy)
            SpeechRecognizer.ERROR_SERVER -> context.getString(R.string.speech_error_server)
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                context.getString(R.string.speech_error_speech_timeout)
            SpeechRecognizer.ERROR_TOO_MANY_REQUESTS ->
                context.getString(R.string.speech_error_too_many_requests)
            SpeechRecognizer.ERROR_SERVER_DISCONNECTED ->
                context.getString(R.string.speech_error_server_disconnected)
            else -> context.getString(R.string.speech_error_unknown, error)
        }
    }

    private fun extractRecognitionData(bundle: Bundle?): RecognitionData? {
        if (bundle == null) return null

        return try {
            val results = bundle
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.toList()
                .orEmpty()

            val confidenceScores = bundle
                .getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                ?.toList()
                .orEmpty()

            val isFinal = bundle.getBoolean("final_result", false)
            val locale = bundle.getString("current_locale").orEmpty()

            RecognitionData(
                results = results,
                confidenceScores = confidenceScores,
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
        private const val RESTART_DELAY_MS = 300L
    }
}
