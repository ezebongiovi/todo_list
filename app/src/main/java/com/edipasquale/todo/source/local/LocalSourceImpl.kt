package com.edipasquale.todo.source.local

import com.edipasquale.todo.db.TaskDao
import com.edipasquale.todo.db.entity.TaskEntity

class LocalSourceImpl(private val dao: TaskDao) : LocalSource {

    override fun createTasks(tasks: List<TaskEntity>) {
        if (tasks.isNotEmpty())
            dao.insertTasks(tasks)
    }

    override fun getTasks(done: Boolean) = dao.getTasks(done)

    override fun getTasksToUpload() = dao.getTasksToUpload()

    override fun getTasksLiveData(done: Boolean) = dao.getTasksLiveData(done)
}