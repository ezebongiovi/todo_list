package com.edipasquale.todo.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.edipasquale.todo.db.entity.TaskEntity

const val DATABASE_NAME = "tasks_database"

@Database(entities = [TaskEntity::class], version = 1)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun tasksDao(): TaskDao
}