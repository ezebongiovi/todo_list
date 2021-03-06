package com.edipasquale.todo.source.local

import com.edipasquale.todo.db.TaskDao
import com.edipasquale.todo.db.entity.TaskEntity
import com.edipasquale.todo.dto.APIError
import com.edipasquale.todo.dto.APIResult

class RoomSourceImpl(private val dao: TaskDao) : LocalTasksSource {

    override suspend fun createTask(task: TaskEntity) = dao.createTask(task)

    override suspend fun getAllTasks() = dao.getAllTasks()

    override fun getAllTasksStream() = dao.getTasksLiveData()

    override suspend fun getUnSyncedTasks() = dao.getTasksToUpload()

    override suspend fun updateTask(task: TaskEntity) = dao.updateTask(task)
}