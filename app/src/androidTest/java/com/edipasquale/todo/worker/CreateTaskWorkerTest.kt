package com.edipasquale.todo.worker

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation
import com.edipasquale.todo.KoinTestApp
import com.edipasquale.todo.db.entity.TaskEntity
import com.edipasquale.todo.dto.APIError
import com.edipasquale.todo.dto.ERROR_GRAPHQL
import com.edipasquale.todo.dto.Failure
import com.edipasquale.todo.dto.Success
import com.edipasquale.todo.source.local.LocalSource
import com.edipasquale.todo.source.network.GraphQLSource
import com.example.todolisttest.CreateTaskMutation
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
class CreateTaskWorkerTest {
    private lateinit var _koinApp: KoinTestApp
    private val _mockLocalSource : LocalSource = mockk()
    private val _mockRemoteSource: GraphQLSource = mockk()

    @Before
    fun setup() {
        _koinApp = ApplicationProvider.getApplicationContext()

        _koinApp.setUpModule(module {
            single { _mockLocalSource }

            single { _mockRemoteSource }
        })
    }

    @Test
    fun noTasksToUpload() {
        val worker = TestListenableWorkerBuilder<CreateTaskWorker>(_koinApp).build()

        /*
            Stub response from local source so it returns an empty list. That means there're no tasks
            to upload to the server.
         */
        every { _mockLocalSource.getTasksToUpload() } returns emptyList()

        val result = worker.startWork().get()

        Assert.assertTrue(result is ListenableWorker.Result.Success)

        // We verify that the remote source has not been called. Since there's nothing to upload
        verify(exactly = 0) { _mockRemoteSource.executeMutation(any<Mutation<Operation.Data, Any, Operation.Variables>>()) }
    }

    @Test
    fun tasksToUploadSuccess() {
        val worker = TestListenableWorkerBuilder<CreateTaskWorker>(_koinApp).build()

        /*
            Stub response from local source so it returns a list. That means there're tasks
            to upload to the server.
         */
        every { _mockLocalSource.getTasksToUpload() } returns listOf(
            TaskEntity(
                name = "Some name",
                note = "Some note"
            )
        )

        /*
            Stub response for local source update. When creating a task on the remote source
            the result is an updated tas which we need to insert on our DB. Updating our existing
            task
         */
        every { _mockLocalSource.createTasks(any()) } returns Unit

        /*
            Stub response from remote source to a success response
         */
        every { _mockRemoteSource.executeMutation(any<Mutation<Operation.Data, CreateTaskMutation.Data, Operation.Variables>>()) }  returns flowOf(
            Success(CreateTaskMutation.Data(
                createTask = CreateTaskMutation.CreateTask(
                    id = "",
                    name = "",
                    note = "",
                    isDone = false
                )
            ))
        )

        val result = worker.startWork().get()

        Assert.assertTrue(result is ListenableWorker.Result.Success)

        // We verify that the remote source has been called. Since there are tasks to upload
        verify(exactly = 1) { _mockRemoteSource.executeMutation(any<Mutation<Operation.Data, CreateTaskMutation.Data, Operation.Variables>>()) }
    }

    @Test
    fun tasksToUploadError() {
        val worker = TestListenableWorkerBuilder<CreateTaskWorker>(_koinApp).build()

        /*
            Stub response from local source so it returns a list. That means there're tasks
            to upload to the server.
         */
        every { _mockLocalSource.getTasksToUpload() } returns listOf(
            TaskEntity(
                name = "Some name",
                note = "Some note"
            )
        )

        /*
            Stub response for local source update. When creating a task on the remote source
            the result is an updated tas which we need to insert on our DB. Updating our existing
            task
         */
        every { _mockLocalSource.createTasks(any()) } returns Unit

        /*
            Stub response from remote source to an error response
         */
        val expectedError = APIError(
            error = ERROR_GRAPHQL,
            errorDescription = "Some description"
        )

        every { _mockRemoteSource.executeMutation(any<Mutation<Operation.Data, CreateTaskMutation.Data, Operation.Variables>>()) }  returns flowOf(
            Failure(expectedError)
        )

        val result = worker.startWork().get()

        // Make sure the worker will retry its work eventually
        Assert.assertTrue(result is ListenableWorker.Result.Retry)

        // We verify that the remote source has been called. Since there are tasks to upload
        verify(exactly = 1) { _mockRemoteSource.executeMutation(any<Mutation<Operation.Data, CreateTaskMutation.Data, Operation.Variables>>()) }
    }
}