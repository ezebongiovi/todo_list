package com.edipasquale.todo.source.local

import androidx.lifecycle.LiveData
import com.edipasquale.todo.db.entity.TaskEntity

interface LocalTasksSource {

    suspend fun createTask(task: TaskEntity): TaskEntity

    suspend fun getAllTasks(): List<TaskEntity>

    suspend fun getUnSyncedTasks(): List<TaskEntity>

    fun getAllTasksStream(): LiveData<List<TaskEntity>>
}