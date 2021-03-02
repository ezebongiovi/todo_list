package com.edipasquale.todo.extensions

import com.edipasquale.todo.db.entity.TaskEntity
import com.example.todolist.CreateTaskMutation

fun CreateTaskMutation.CreateTask.toTaskEntity(_id: Long = 0): TaskEntity = TaskEntity(
    _id = _id,
    id = id,
    name = name,
    note = note ?: "",
    isDone = isDone
)