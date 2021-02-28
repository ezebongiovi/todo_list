package com.edipasquale.todo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.edipasquale.todo.db.entity.TaskEntity
import com.edipasquale.todo.dto.APIError
import com.edipasquale.todo.repository.TasksRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TasksViewModel(
    app: Application,
    private val _repository: TasksRepository
) : AndroidViewModel(app) {

    private val _error = MutableLiveData<APIError>()
    val error: LiveData<APIError> get() = _error
    val tasks: LiveData<List<TaskEntity>> get() = _repository.getTasks(false)

    fun pullToRefresh() = viewModelScope.launch(Dispatchers.IO) {
        _error.postValue(_repository.getTasksFromNetwork())
    }
}