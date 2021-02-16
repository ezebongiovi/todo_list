package com.edipasquale.todo.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.*
import com.edipasquale.todo.db.entity.TaskEntity
import com.edipasquale.todo.dto.APIError
import com.edipasquale.todo.dto.APIResult
import com.edipasquale.todo.dto.Failure
import com.edipasquale.todo.dto.Success
import com.edipasquale.todo.repository.TasksRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class TasksViewModel(
    app: Application,
    private val _repository: TasksRepository
) : AndroidViewModel(app) {
    private val _error = MutableLiveData<APIError>()
    val error: LiveData<APIError> = _error
    val tasks: LiveData<List<TaskEntity>> = _repository.getTasks(done = false).map {
            handleResult(it)
        }

    fun pullToRefresh() = viewModelScope.launch {
        _repository.getTasksFromServer().collect {
            _error.postValue(it)
        }
    }

    @SuppressLint("NullSafeMutableLiveData")
    private fun handleResult(it: APIResult<List<TaskEntity>, APIError>): List<TaskEntity> {
        return when (it) {
            is Success -> {
                it.value
            }

            is Failure -> {
                _error.postValue(it.reason)

                tasks.value ?: emptyList()
            }
        }
    }
}