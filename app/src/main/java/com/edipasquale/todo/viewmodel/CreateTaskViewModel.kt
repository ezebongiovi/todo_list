package com.edipasquale.todo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.edipasquale.todo.db.entity.TaskEntity
import com.edipasquale.todo.repository.TasksRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreateTaskViewModel(
    app: Application,
    private val _repository: TasksRepository
) : AndroidViewModel(app) {

    private val _taskCreation = MutableLiveData<TaskEntity>()
    val taskCreation : LiveData<TaskEntity> = _taskCreation

    fun createTask(name: String, note: String) = viewModelScope.launch(Dispatchers.IO) {
        val entity = TaskEntity(
            name = name,
            note = note
        )

        _repository.createTasks(entity)

        _taskCreation.postValue(entity)
    }
}