package com.shkarov.mytasks.data.speech

data class RecognitionData(
    val results: List<String>,
    val confidenceScores: List<Float>,
    val isFinal: Boolean,
    val locale: String
)
