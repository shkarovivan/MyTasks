package com.shkarov.mytasks.screens

import androidx.compose.runtime.Composable
import com.shkarov.mytasks.Greeting

@Composable
fun DetailTaskScreen(
    taskId: String,
    onBackClick: () -> Unit
){
    Greeting(name = taskId)
}