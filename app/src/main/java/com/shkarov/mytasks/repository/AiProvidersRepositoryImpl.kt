package com.shkarov.mytasks.repository

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiProvidersRepositoryImpl @Inject constructor(): AiProvidersRepository {
    private val providers: List<AiProvider> = listOf(
        proxyAi,
//        eliza
    )

    override fun getAiProviders(): List<AiProvider> {
        return providers
    }

    companion object {
        private val proxyAi: AiProvider = AiProvider(
            name = "ProxyAi",
            host = "https://api.proxyapi.ru",
            models = listOf(
                AiModel("GPT-5.4 mini", "gpt-5.4-mini"),
                AiModel("GPT-o4 mini", "o4-mini"),
                AiModel("GPT-5.4", "gpt-5.4"),
//                AiModel("Claude Opus 4.6", "claude-opus-4-6"),
//                AiModel("Claude Sonnet 4.6", "claude-sonnet-4-6"),
//                AiModel("Gemini 2.5 pro", "gemini-2.5-pro"),
            )
        )

        private val eliza: AiProvider = AiProvider(
            name = "Eliza",
            host = "",
            models = listOf(

            )
        )
    }
}