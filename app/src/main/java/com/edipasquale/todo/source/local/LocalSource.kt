package com.edipasquale.todo.source.local

import androidx.lifecycle.LiveData
import com.edipasquale.todo.db.entity.TaskEntity

interface LocalSource {

    /**
     * @param tasks the tasks being created
     *
     * @return the just created tasks
     */
    fun createTasks(tasks: List<TaskEntity>)

    /**
     * @param done whether to retrieve done tasks or not
     *
     * @return the list of created tasks
     */
    fun getTasks(done: Boolean): List<TaskEntity>

    /**
     * Fetches from local source all those tasks that are considered as not-synced with backend
     *
     * @return the list of not-synced tasks
     */
    fun getTasksToUpload() : List<TaskEntity>

    /**
     * Subscribes to table changes and emits the updated list of tasks
     *
     * @param done whether to retrieve done tasks or not
     *
     * @return the list of tasks as [LiveData]
     */
    fun getTasksLiveData(done: Boolean) : LiveData<List<TaskEntity>>
}