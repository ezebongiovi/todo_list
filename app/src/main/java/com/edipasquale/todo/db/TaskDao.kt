package com.edipasquale.todo.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.edipasquale.todo.db.entity.TaskEntity

@Dao
abstract class TaskDao {

    @Transaction
    open fun createTask(task: TaskEntity): TaskEntity {
        return task.copy(_id = insertTask(task))
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun insertTask(task: TaskEntity): Long

    @Query("SELECT * FROM tasks ORDER BY _id DESC")
    abstract fun getAllTasks(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE isDone = :done")
    abstract fun getTasks(done: Boolean): List<TaskEntity>

    @Query("SELECT * FROM tasks")
    abstract fun getTasksLiveData(): LiveData<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id is null")
    abstract fun getTasksToUpload(): List<TaskEntity>

    @Update
    protected abstract fun updateTaskEntity(task: TaskEntity): Int

    @Transaction
    open fun updateTask(updatedTask: TaskEntity): TaskEntity {
        updateTaskEntity(updatedTask)

        return updatedTask
    }
}