package com.edipasquale.todo.worker

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.edipasquale.todo.KoinTestApp
import com.edipasquale.todo.db.entity.TaskEntity
import com.edipasquale.todo.dto.APIError
import com.edipasquale.todo.dto.ERROR_GRAPHQL
import com.edipasquale.todo.dto.Failure
import com.edipasquale.todo.dto.Success
import com.edipasquale.todo.source.local.LocalTasksSource
import com.edipasquale.todo.source.network.tasks.NetworkTasksSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.dsl.bind
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
class LocalSyncWorkerTest {
    private lateinit var _koinApp: KoinTestApp
    private val _mockLocalSource: LocalTasksSource = mockk()
    private val _mockRemoteSource: NetworkTasksSource = mockk()

    @Before
    fun setup() {
        _koinApp = ApplicationProvider.getApplicationContext()

        _koinApp.setUpModule(module {
            factory(override = true) { _mockLocalSource } bind LocalTasksSource::class
            factory(override = true) { _mockRemoteSource } bind NetworkTasksSource::class
        })
    }

    @Test
    fun noTasksToUpload() {
        // Preparation
        val worker = TestListenableWorkerBuilder<LocalSyncWorker>(_koinApp).build()
        coEvery { _mockLocalSource.getUnSyncedTasks() } returns emptyList()

        // Execution
        val result = worker.startWork().get()

        // Verification
        Assert.assertTrue(result is ListenableWorker.Result.Success)
        coVerify(exactly = 0) { _mockRemoteSource.createTask(any()) }
    }

    @Test
    fun tasksToUploadSuccess() {
        // Preparation
        val worker = TestListenableWorkerBuilder<LocalSyncWorker>(_koinApp).build()
        val entity = TaskEntity(name = "Some name", note = "Some note")
        coEvery { _mockLocalSource.getUnSyncedTasks() } returns listOf(entity)
        coEvery { _mockLocalSource.createTask(any()) } returns entity
        coEvery { _mockLocalSource.updateTask(any()) } returns entity
        coEvery { _mockRemoteSource.createTask(any()) } coAnswers {
            Success(TaskEntity(id = "", name = "", note = "", isDone = false))
        }

        // Execution
        val result = worker.startWork().get()

        // Verification
        Assert.assertTrue(result is ListenableWorker.Result.Success)
        coVerify(exactly = 1) { _mockRemoteSource.createTask(any()) }
    }

    @Test
    fun tasksToUploadError() {
        // Preparation
        val worker = TestListenableWorkerBuilder<LocalSyncWorker>(_koinApp).build()
        val entity = TaskEntity(name = "Some name", note = "Some note")
        val expectedError = APIError(error = ERROR_GRAPHQL, errorDescription = "Some description")
        coEvery { _mockLocalSource.getUnSyncedTasks() } returns listOf(entity)
        coEvery { _mockLocalSource.createTask(any()) } returns entity
        coEvery { _mockRemoteSource.createTask(entity) } coAnswers { Failure(expectedError) }

        // Execution
        val result = worker.startWork().get()

        // Verification
        Assert.assertTrue(result is ListenableWorker.Result.Retry)
        coVerify(exactly = 1) { _mockRemoteSource.createTask(any()) }
    }
}