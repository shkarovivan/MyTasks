package com.shkarov.mytasks.viewmodels

import androidx.lifecycle.ViewModel
import com.shkarov.mytasks.repository.TasksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddTaskViewModel @Inject constructor(
    private val repository: TasksRepository
): ViewModel() {

}