package com.edipasquale.todo.source.local

import androidx.lifecycle.LiveData
import com.edipasquale.todo.db.entity.TaskEntity
import com.edipasquale.todo.dto.APIError
import com.edipasquale.todo.dto.APIResult

interface LocalTasksSource {

    suspend fun createTask(task: TaskEntity): TaskEntity

    suspend fun getAllTasks(): List<TaskEntity>

    suspend fun getUnSyncedTasks(): List<TaskEntity>

    suspend fun updateTask(task: TaskEntity): TaskEntity

    fun getAllTasksStream(): LiveData<List<TaskEntity>>
}