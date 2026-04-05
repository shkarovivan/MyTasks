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
                AiModel("openai/gpt-5.4"),
                AiModel("openai/o4-mini"),
                AiModel("anthropic/claude-opus-4-6"),
                AiModel("anthropic/claude-sonnet-4-6"),
                AiModel("gemini/gemini-2.5-pro"),
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