package com.edipasquale.todo.source.network.tasks

import com.edipasquale.todo.db.entity.TaskEntity
import com.edipasquale.todo.dto.APIError
import com.edipasquale.todo.dto.APIResult

interface NetworkTasksSource {

    suspend fun createTask(task: TaskEntity): APIResult<TaskEntity, APIError>

    suspend fun getAllTasks(): APIResult<List<TaskEntity>, APIError>
}