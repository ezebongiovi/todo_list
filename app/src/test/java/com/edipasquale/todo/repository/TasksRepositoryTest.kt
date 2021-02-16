package com.edipasquale.todo.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Query
import com.edipasquale.todo.db.entity.TaskEntity
import com.edipasquale.todo.dto.APIError
import com.edipasquale.todo.dto.Failure
import com.edipasquale.todo.dto.Success
import com.edipasquale.todo.ext.getOrAwaitValue
import com.edipasquale.todo.source.local.LocalSource
import com.edipasquale.todo.source.network.GraphQLSource
import com.example.todolist.GetAllTasksQuery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.*

class TasksRepositoryTest {
    private val _testCoroutineDispatcher = TestCoroutineDispatcher()
    private val _testCoroutineScope = TestCoroutineScope(_testCoroutineDispatcher)
    private val _mockedLocalSource = mockk<LocalSource>()
    private val _mockedRemoteSource = mockk<GraphQLSource>()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        Dispatchers.setMain(_testCoroutineDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun createTask() = _testCoroutineScope.runBlockingTest {
        val repository = TasksRepository(
            _mockedLocalSource,
            _mockedRemoteSource,
            _testCoroutineDispatcher
        )

        // Stub local response
        every { _mockedLocalSource.createTasks(any()) } returns Unit

        repository.createTask(TaskEntity(name = ""))

        verify(exactly = 1) { _mockedLocalSource.createTasks(any()) }
    }

    @Test
    fun `Get empty list of tasks from local only`() = _testCoroutineScope.runBlockingTest {
        val repository = TasksRepository(
            _mockedLocalSource,
            _mockedRemoteSource,
            _testCoroutineDispatcher
        )

        // Stub local response
        every { _mockedLocalSource.getTasksLiveData(any()) } returns MutableLiveData<List<TaskEntity>>().apply {
            value = emptyList()
        }

        repository.getTasks(done = true).getOrAwaitValue()

        verify(exactly = 0) { _mockedRemoteSource.executeQuery(any<Query<Operation.Data, Any, Operation.Variables>>()) }
    }

    @Test
    fun `Force refresh Success`() = _testCoroutineScope.runBlockingTest {
        val repository = TasksRepository(
            _mockedLocalSource,
            _mockedRemoteSource,
            _testCoroutineDispatcher
        )

        // Stub local response
        every { _mockedLocalSource.getTasksLiveData(any()) } returns MutableLiveData<List<TaskEntity>>().apply {
            value = emptyList()
        }

        /*
            When the refresh succeeds it will update the local database. So we need to stub
            the process of inserting tasks
         */
        every { _mockedLocalSource.createTasks(any()) } returns Unit

        // Stubs a response from server with a list of items
        every { _mockedRemoteSource.executeQuery(any<Query<Operation.Data, GetAllTasksQuery.Data, Operation.Variables>>()) } returns flowOf(
            Success(
                GetAllTasksQuery.Data(
                    allTasks = listOf(
                        GetAllTasksQuery.AllTask(
                            __typename = "",
                            name = "",
                            id = "",
                            isDone = false,
                            note = ""
                        )
                    )
                )
            )
        )

        val result = repository.getTasksFromServer().firstOrNull()

        Assert.assertNull(result)

        verify(exactly = 1) { _mockedRemoteSource.executeQuery(any<Query<Operation.Data, Any, Operation.Variables>>()) }
    }

    @Test
    fun `Force refresh Error`() = _testCoroutineScope.runBlockingTest {
        val repository = TasksRepository(
            _mockedLocalSource,
            _mockedRemoteSource,
            _testCoroutineDispatcher
        )

        // Stub local response
        every { _mockedLocalSource.getTasksLiveData(any()) } returns MutableLiveData<List<TaskEntity>>().apply {
            value = emptyList()
        }

        /*
            When the refresh succeeds it will update the local database. So we need to stub
            the process of inserting tasks
         */
        every { _mockedLocalSource.createTasks(any()) } returns Unit

        // Stubs a response from server with a list of items
        every { _mockedRemoteSource.executeQuery(any<Query<Operation.Data, GetAllTasksQuery.Data, Operation.Variables>>()) } returns flowOf(
            Failure(APIError("Error"))
        )

        val result = repository.getTasksFromServer().firstOrNull()

        Assert.assertNotNull(result)

        verify(exactly = 1) { _mockedRemoteSource.executeQuery(any<Query<Operation.Data, Any, Operation.Variables>>()) }
    }
}