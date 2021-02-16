package com.edipasquale.todo.repository

import androidx.lifecycle.liveData
import androidx.lifecycle.map
import com.edipasquale.todo.db.entity.TaskEntity
import com.edipasquale.todo.dto.Failure
import com.edipasquale.todo.dto.Success
import com.edipasquale.todo.source.local.LocalSource
import com.edipasquale.todo.source.network.GraphQLSource
import com.example.todolisttest.GetAllTasksQuery
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect

class TasksRepository(
    private val _localSource: LocalSource,
    private val _remoteSource: GraphQLSource,
    private val _coroutineContext: CoroutineDispatcher = Dispatchers.IO
) {

    fun createTask(task: TaskEntity) {
        _localSource.createTasks(listOf(task))
    }

    fun getTasks(done: Boolean, forceRefresh: Boolean) = liveData(context = _coroutineContext) {
        // Subscribe to local database changes
        emitSource(_localSource.getTasksLiveData(done).map {
            Success(it)
        })

        // Make a refresh from the remote source
        if (forceRefresh)
            _remoteSource.executeQuery(GetAllTasksQuery()).collect {
                when (it) {
                    is Success -> {
                        // If there're tasks, save them on the local source
                        it.value.tasksAsEntities()?.let { tasks ->
                            _localSource.createTasks(tasks)
                        }
                    }
                    is Failure -> {
                        // Propagate the error
                        emit(it)
                    }
                }
            }
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