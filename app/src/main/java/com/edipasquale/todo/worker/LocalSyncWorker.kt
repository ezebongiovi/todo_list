package com.edipasquale.todo.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import com.edipasquale.todo.db.entity.TaskEntity
import com.edipasquale.todo.dto.APIError
import com.edipasquale.todo.dto.Failure
import com.edipasquale.todo.dto.Success
import com.edipasquale.todo.source.network.tasks.NetworkTasksSource
import com.edipasquale.todo.source.local.LocalTasksSource
import org.koin.java.KoinJavaComponent

class LocalSyncWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val _remoteSource by KoinJavaComponent.inject(clazz = NetworkTasksSource::class.java)
    private val _localSource by KoinJavaComponent.inject(clazz = LocalTasksSource::class.java)

    override suspend fun doWork(): Result {
        val tasks = _localSource.getUnSyncedTasks()

        tasks.forEach { localTask ->
            return when (val response = _remoteSource.createTask(localTask)) {
                is Success -> onRemoteCreationSuccess(localTask, response.value)
                is Failure -> onRemoteCreationFailure(response.reason)
            }
        }

        return Result.success()
    }

    private suspend fun onRemoteCreationSuccess(
        localTask: TaskEntity,
        remoteTask: TaskEntity
    ): Result {
        val entity = TaskEntity(
            _id = localTask._id,
            id = remoteTask.id,
            name = remoteTask.name,
            note = remoteTask.note,
            isDone = remoteTask.isDone
        )

        _localSource.updateTask(entity)

        return Result.success()
    }

    private fun onRemoteCreationFailure(reason: APIError): Result {
        Log.e(this::class.java.simpleName, "${reason.error} - ${reason.errorDescription}")

        return Result.retry()
    }

    companion object {
        fun oneTimeWorkRequest() = OneTimeWorkRequestBuilder<LocalSyncWorker>()
                .setConstraints(
                    Constraints
                        .Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
    }
}