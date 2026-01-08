package com.shkarov.mytasks.network.data

data class ChatRequest(
    val model: String = "gpt-4o",
    val messages: List<ChatMessage>
)