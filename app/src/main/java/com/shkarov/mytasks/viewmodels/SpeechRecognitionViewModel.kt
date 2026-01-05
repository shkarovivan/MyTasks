package com.shkarov.mytasks.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shkarov.mytasks.R
import com.shkarov.mytasks.data.speech.SpeechRecognition
import com.shkarov.mytasks.data.speech.SpeechRecognitionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SpeechRecognitionViewModel @Inject constructor(
    application: Application,
    private val speechRecognition: SpeechRecognition
) : AndroidViewModel(application) {

    private val _recognizedText = MutableStateFlow("")
    val recognizedText = _recognizedText.asStateFlow()

    private val _statusText = MutableStateFlow(application.getString(R.string.voice_stopped))
    val statusText = _statusText.asStateFlow()

    private val _loadProgress = MutableStateFlow(false)
    val loadProgress = _loadProgress.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.Main) {
            speechRecognition.recognitionState
                .collect { state ->
                    Timber.d("$TAG recognitionState - $state")
                    when (state) {
                        is SpeechRecognitionState.Initializing -> {
                            _statusText.value = application.getString(R.string.voice_initialization)
                        }

                        is SpeechRecognitionState.Ready -> {
                            _statusText.value = application.getString(R.string.voice_listening)
                        }

                        is SpeechRecognitionState.Listening -> {
                            _statusText.value = application.getString(R.string.voice_listening)
                            _loadProgress.value = false
                        }

                        is SpeechRecognitionState.PartialResult -> {
                            Timber.d("${TAG} onPartialResults data - ${state.text}")
                            _recognizedText.value = state.text
                        }

                        is SpeechRecognitionState.FinalResult -> {
                            _recognizedText.value = "✅ ${state.text}"
                            _loadProgress.value = true
                            _statusText.value = application.getString(R.string.voice_start)
                        }

                        is SpeechRecognitionState.Error -> {
                            _recognizedText.value = "❌ ${state.message}"
                            _loadProgress.value = true
                            _statusText.value = application.getString(R.string.voice_start)
                        }

                        is SpeechRecognitionState.PermissionDenied -> {
                            _statusText.value = application.getString(R.string.voice_permission)
                            _loadProgress.value = false
                        }

                        is SpeechRecognitionState.SpeechEnded,
                        SpeechRecognitionState.Stopped -> {
                            _statusText.value = application.getString(R.string.voice_stopped)
                        }
                    }
                }
        }
    }

    fun startRecord() {
        _recognizedText.value = ""
        speechRecognition.startRecognize()
    }

    fun stopRecognition() {
        speechRecognition.stopRecognition()
        _recognizedText.value = ""
    }

    companion object {
        private const val TAG = "SpeechRecognitionViewModel"
    }
}