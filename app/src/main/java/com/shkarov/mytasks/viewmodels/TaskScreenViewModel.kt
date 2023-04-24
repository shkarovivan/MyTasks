package com.shkarov.mytasks.viewmodels

import androidx.lifecycle.ViewModel
import com.shkarov.mytasks.data.Status
import com.shkarov.mytasks.data.Task
import com.shkarov.mytasks.repository.TasksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class TaskScreenViewModel @Inject constructor(
    private val repository: TasksRepository
) : ViewModel() {
    var errorString = MutableStateFlow<String?>(null)
    var loadProgress = MutableStateFlow(false)
    val dailyTasks = MutableStateFlow<List<Task>>(emptyList())
    val mediumTasks = MutableStateFlow<List<Task>>(emptyList())
    val largeTasks = MutableStateFlow<List<Task>>(emptyList())

    init {
        getDailyTasks()
        getMediumTasks()
        getLargeTasks()
    }

    private fun getDailyTasks() {
        val task = Task(
            id = "0",
            created = "21.04.2023",
            title = "Проверить  работу",
            description = "Проверить работу TaskView в превью приложения и попарвить при необходимости",
            type = "daily",
            deadLine = "21.04.2023",
            deadLineMs = 1,
            status = Status.STARTED
        )
        val list = mutableListOf<Task>()
        (1..12).forEach {
            list.add(task.copy(id = it.toString()))
        }
        dailyTasks.value =  list
    }

    private fun getMediumTasks(){
        mediumTasks.value =  emptyList()
    }
    private fun getLargeTasks(){
        largeTasks.value =  emptyList()
    }
}