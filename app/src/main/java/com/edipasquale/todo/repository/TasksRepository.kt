package com.edipasquale.todo.repository

import androidx.lifecycle.liveData
import androidx.lifecycle.map
import com.edipasquale.todo.db.entity.TaskEntity
import com.edipasquale.todo.dto.APIError
import com.edipasquale.todo.dto.APIResult
import com.edipasquale.todo.dto.Failure
import com.edipasquale.todo.dto.Success
import com.edipasquale.todo.source.local.LocalSource
import com.edipasquale.todo.source.network.GraphQLSource
import com.example.todolist.GetAllTasksQuery
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class TasksRepository(
    private val _localSource: LocalSource,
    private val _remoteSource: GraphQLSource,
    private val _coroutineContext: CoroutineDispatcher = Dispatchers.IO
) {

    fun createTask(task: TaskEntity) {
        _localSource.createTasks(listOf(task))
    }

    fun getTasks(done: Boolean) = liveData(context = _coroutineContext) {
        // Subscribe to local database changes
        emitSource(_localSource.getTasksLiveData(done).map {
            Success(it)
        })
    }

    fun getTasksFromServer() = flow<APIError?> {
        emitAll(_remoteSource.executeQuery(GetAllTasksQuery()).map {
            when (it) {
                is Success -> {
                    // If there're tasks, save them on the local source
                    it.value.tasksAsEntities()?.let { tasks ->
                        _localSource.createTasks(tasks)
                    }

                    null
                }
                is Failure -> {
                    // Propagate the error
                    it.reason
                }
            }
        })
    }

    private fun GetAllTasksQuery.Data.tasksAsEntities(): List<TaskEntity>? {
        return allTasks?.filterNotNull()?.map {
            TaskEntity(
                name = it.name,
                note = it.note ?: "",
                isDone = it.isDone,
                id = it.id
            )
        }
    }
}