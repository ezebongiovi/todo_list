package com.edipasquale.todo.extensions

import com.edipasquale.todo.db.entity.TaskEntity
import com.example.todolist.GetAllTasksQuery

fun GetAllTasksQuery.Data.tasksAsEntities(): List<TaskEntity> {
    return allTasks?.filterNotNull()?.map {
        TaskEntity(
            name = it.name,
            note = it.note ?: "",
            isDone = it.isDone,
            id = it.id
        )
    } ?: emptyList()
}