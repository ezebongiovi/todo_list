package com.edipasquale.todo.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.edipasquale.todo.db.entity.TaskEntity

@Dao
abstract class TaskDao {

    @Transaction
    open fun insertTasks(tasks: List<TaskEntity>): List<TaskEntity> {
        return tasks.map { task ->
            task.copy(_id = insertTask(task))
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun insertTask(task: TaskEntity): Long

    @Query("SELECT * FROM tasks")
    abstract fun getAllTasks(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE isDone = :done")
    abstract fun getTasks(done: Boolean): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE isDone = :done")
    abstract fun getTasksLiveData(done: Boolean): LiveData<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id is null")
    abstract fun getTasksToUpload(): List<TaskEntity>
}