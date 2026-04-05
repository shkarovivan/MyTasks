package com.shkarov.mytasks.repository

interface AiProvidersRepository {
    fun getAiProviders(): List<AiProvider>
}

data class AiProvider(
    val name: String,
    val host: String,
    val models: List<AiModel>
)

data class AiModel(
    val name: String,
    val path: String,
)