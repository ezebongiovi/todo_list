package com.edipasquale.todo.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.edipasquale.todo.db.entity.TaskEntity
import com.edipasquale.todo.dto.APIError
import com.edipasquale.todo.dto.Failure
import com.edipasquale.todo.dto.Success
import com.edipasquale.todo.ext.getOrAwaitValue
import com.edipasquale.todo.source.NetworkTasksSource
import com.edipasquale.todo.source.local.LocalTasksSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.*

class TasksRepositoryTest {
    private val _testCoroutineDispatcher = TestCoroutineDispatcher()
    private val _testCoroutineScope = TestCoroutineScope(_testCoroutineDispatcher)
    private val _mockedLocalSource = mockk<LocalTasksSource>()
    private val _mockedRemoteSource = mockk<NetworkTasksSource>()

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
            _mockedRemoteSource
        )

        // Stub local response
        coEvery { _mockedLocalSource.createTask(any()) } returns mockk()

        repository.createTasks(TaskEntity(name = ""))

        coVerify(exactly = 1) { _mockedLocalSource.createTask(any()) }
    }

    @Test
    fun `Get empty list of tasks from local only`() = _testCoroutineScope.runBlockingTest {
        val repository = TasksRepository(
            _mockedLocalSource,
            _mockedRemoteSource
        )

        // Stub local response
        every { _mockedLocalSource.getAllTasksStream() } returns MutableLiveData<List<TaskEntity>>().apply {
            value = emptyList()
        }

        repository.getTasks().getOrAwaitValue()

        coVerify(exactly = 0) { _mockedRemoteSource.createTask(any()) }
    }

    @Test
    fun `Force refresh Success`() = _testCoroutineScope.runBlockingTest {
        val repository = TasksRepository(
            _mockedLocalSource,
            _mockedRemoteSource
        )

        // Stub local response
        every { _mockedLocalSource.getAllTasksStream() } returns MutableLiveData<List<TaskEntity>>().apply {
            value = emptyList()
        }

        /*
            When the refresh succeeds it will update the local database. So we need to stub
            the process of inserting tasks
         */
        coEvery { _mockedLocalSource.createTask(any()) } returns mockk()

        // Stubs a response from server with a list of items
        coEvery { _mockedRemoteSource.getAllTasks() } coAnswers {
            Success(listOf())
        }

        val result = repository.getTasksFromNetwork()

        Assert.assertNull(result)

        coVerify(exactly = 1) { _mockedRemoteSource.getAllTasks() }
    }

    @Test
    fun `Force refresh Error`() = _testCoroutineScope.runBlockingTest {
        val repository = TasksRepository(
            _mockedLocalSource,
            _mockedRemoteSource
        )

        // Stub local response
        every { _mockedLocalSource.getAllTasksStream() } returns MutableLiveData<List<TaskEntity>>().apply {
            value = emptyList()
        }

        /*
            When the refresh succeeds it will update the local database. So we need to stub
            the process of inserting tasks
         */
        coEvery { _mockedLocalSource.createTask(any()) } returns mockk()

        // Stubs a response from server with a list of items
        coEvery { _mockedRemoteSource.getAllTasks() } coAnswers {
            Failure(APIError("Error"))
        }

        val result = repository.getTasksFromNetwork()

        Assert.assertNotNull(result)

        coVerify(exactly = 1) { _mockedRemoteSource.getAllTasks() }
    }
}