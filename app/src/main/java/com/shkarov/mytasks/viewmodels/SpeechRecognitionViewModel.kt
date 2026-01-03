package com.shkarov.mytasks.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val speechRecognition: SpeechRecognition
) : ViewModel() {

    private val _recognizedText = MutableStateFlow("")
    val recognizedText = _recognizedText.asStateFlow()

    private val _statusText = MutableStateFlow("")
    val statusText = _statusText.asStateFlow()

    private val _loadProgress = MutableStateFlow(false)
    val loadProgress = _loadProgress.asStateFlow()

    fun stopRecord() {
        speechRecognition.stopRecognition()
    }

    init {
        viewModelScope.launch(Dispatchers.Main) {
            speechRecognition.startRecognize()
            Timber.d("$TAG startRecognize()")
            speechRecognition.recognitionState
                .collect { state ->
                    Timber.d("$TAG recognitionState - $state")
                    when (state) {
                        is SpeechRecognitionState.Initializing -> {
                            _statusText.value = "Инициализация.."
                        }

                        is SpeechRecognitionState.Ready -> {
                            _statusText.value = "Готов к записи..."
                        }

                        is SpeechRecognitionState.Listening -> {
                            _statusText.value = "Слушаю..."
                            _loadProgress.value = false
                        }

                        is SpeechRecognitionState.PartialResult -> {

                            Timber.d("${TAG} onPartialResults data - ${state.text}")

                            _recognizedText.value = state.text
                        }

                        is SpeechRecognitionState.FinalResult -> {
                            _recognizedText.value = "✅ ${state.text}"
                            _loadProgress.value = true
                            _statusText.value = "Начать запись"
                        }

                        is SpeechRecognitionState.Error -> {
                            _recognizedText.value = "❌ ${state.message}"
                            _loadProgress.value = true
                            _statusText.value = "Начать запись"
                        }

                        is SpeechRecognitionState.PermissionDenied -> {
                            _statusText.value = "Не разрешения на запись"
                            _loadProgress.value = false
                        }

                        is SpeechRecognitionState.SpeechEnded,
                        SpeechRecognitionState.Stopped -> {
                        }
                    }
                }
        }
    }

    fun startRecord() {
        speechRecognition.startRecognize()
    }

    companion object {
        private const val TAG = "SpeechRecognitionViewModel"
    }
}