package com.shkarov.mytasks.repository

import org.json.JSONObject

interface AiTaskRepository {
    fun sendAiRequest(request: String, prompt: String): JSONObject
}