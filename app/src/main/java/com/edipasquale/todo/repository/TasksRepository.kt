package com.edipasquale.todo.repository

import android.content.Context
import com.edipasquale.todo.db.entity.TaskEntity
import com.edipasquale.todo.dto.APIError
import com.edipasquale.todo.dto.Failure
import com.edipasquale.todo.dto.Success
import com.edipasquale.todo.source.NetworkTasksSource
import com.edipasquale.todo.source.local.LocalTasksSource

class TasksRepository(
    private val _localSource: LocalTasksSource,
    private val _remoteSource: NetworkTasksSource
) {

    suspend fun createTasks(task: TaskEntity) {
        _localSource.createTask(task)
    }

    fun getTasks() = _localSource.getAllTasksStream()

    suspend fun getTasksFromNetwork(): APIError? {
        return when (val response = _remoteSource.getAllTasks()) {
            is Failure -> response.reason
            is Success -> updateLocalDatabase(response.value)
        }
    }

    private suspend fun updateLocalDatabase(newTasks: List<TaskEntity>): Nothing? {
        newTasks.forEach { task ->
            _localSource.createTask(task)
        }

        return null
    }
}