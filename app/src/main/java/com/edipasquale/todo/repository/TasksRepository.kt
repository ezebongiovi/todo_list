package com.edipasquale.todo.repository

import com.edipasquale.todo.db.entity.TaskEntity
import com.edipasquale.todo.dto.APIError
import com.edipasquale.todo.dto.Failure
import com.edipasquale.todo.dto.Success
import com.edipasquale.todo.extensions.tasksAsEntities
import com.edipasquale.todo.source.local.LocalSource
import com.edipasquale.todo.source.network.GraphQLSource
import com.example.todolist.GetAllTasksQuery

class TasksRepository(
    private val _localSource: LocalSource,
    private val _remoteSource: GraphQLSource
) {

    fun createTasks(task: TaskEntity) {
        _localSource.createTasks(listOf(task))
    }

    fun getTasks(done: Boolean) = _localSource.getTasksLiveData(done)

    suspend fun getTasksFromNetwork(): APIError? {
        return when (val response = _remoteSource.executeQuery(GetAllTasksQuery())) {
            is Failure -> response.reason
            is Success -> {
                _localSource.createTasks(response.value.tasksAsEntities())

                null
            }
        }
    }
}