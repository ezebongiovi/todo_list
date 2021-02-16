package com.edipasquale.todo.worker

import android.content.Context
import androidx.work.*
import com.edipasquale.todo.db.entity.TaskEntity
import com.edipasquale.todo.dto.Failure
import com.edipasquale.todo.dto.Success
import com.edipasquale.todo.source.local.LocalSource
import com.edipasquale.todo.source.network.GraphQLSource
import com.example.todolist.CreateTaskMutation
import kotlinx.coroutines.flow.first
import org.koin.java.KoinJavaComponent

class CreateTaskWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    companion object {
        fun schedule(context: Context) {
            WorkManager.getInstance(context).enqueue(
                OneTimeWorkRequestBuilder<CreateTaskWorker>()
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    ).build()
            )
        }
    }

    private val _remoteSource: GraphQLSource by KoinJavaComponent.inject(clazz = GraphQLSource::class.java)
    private val _localSource: LocalSource by KoinJavaComponent.inject(clazz = LocalSource::class.java)

    override suspend fun doWork(): Result {
        val tasks = _localSource.getTasksToUpload()

        // For each task we sync with backend
        tasks.forEach { task ->
            val response = _remoteSource.executeMutation(
                CreateTaskMutation(
                    name = task.name,
                    note = task.note,
                    isDone = task.isDone
                )
            ).first()

            when (response) {
                is Success -> {
                    val data = response.value

                    // Update local database
                    data.createTask?.let { newTask ->
                        _localSource.createTasks(
                            listOf(
                                TaskEntity(
                                    _id = task._id,
                                    id = newTask.id,
                                    name = newTask.name,
                                    note = newTask.note ?: "",
                                    isDone = newTask.isDone
                                )
                            )
                        )
                    }
                }

                is Failure -> {
                    return Result.retry()
                }
            }
        }

        return Result.success()
    }
}