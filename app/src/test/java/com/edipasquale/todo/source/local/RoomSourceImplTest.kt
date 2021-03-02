package com.edipasquale.todo.source.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.edipasquale.todo.db.TaskDao
import com.edipasquale.todo.db.entity.TaskEntity
import com.edipasquale.todo.ext.getOrAwaitValue
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class RoomSourceImplTest {
    private val _testCoroutineDispatcher = TestCoroutineDispatcher()
    private val _testCoroutineScope = TestCoroutineScope(_testCoroutineDispatcher)
    private val _mockedDao: TaskDao = mockk()
    private val _source = RoomSourceImpl(_mockedDao)

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        Dispatchers.setMain(_testCoroutineDispatcher)

        every { _mockedDao.createTask(any()) } returns mockk()
        every { _mockedDao.getAllTasks() } returns emptyList()
        every { _mockedDao.getTasks(any()) } returns emptyList()
        every { _mockedDao.getTasksToUpload() } returns emptyList()
        every { _mockedDao.getTasksLiveData() } returns MutableLiveData<List<TaskEntity>>().apply {
            value = emptyList()
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun createTasks() = _testCoroutineScope.runBlockingTest {
        // Insert a list of tags
        val task = TaskEntity(name = "Task one")
        _source.createTask(task)

        // Verify the TasksDao is being called with the same list
        verify(exactly = 1) { _mockedDao.createTask(task) }
    }

    @Test
    fun getTasks() = _testCoroutineScope.runBlockingTest {
        // Get all undone tasks
        _source.getAllTasks()

        // Verify the TasksDao has been called correctly
        verify(exactly = 1) { _mockedDao.getAllTasks() }
    }

    @Test
    fun getPendingTasks() = _testCoroutineScope.runBlockingTest {
        // Get all done tasks
        _source.getAllTasks()

        // Verify the TasksDao has been called correctly
        verify(exactly = 1) { _mockedDao.getAllTasks() }
    }

    @Test
    fun getTasksLiveData() = _testCoroutineScope.runBlockingTest {
        _source.getAllTasksStream().getOrAwaitValue()

        verify(exactly = 1) { _mockedDao.getTasksLiveData() }
    }

    @Test
    fun getTasksToUpload() = _testCoroutineScope.runBlockingTest {
        _source.getUnSyncedTasks()

        verify(exactly = 1) { _mockedDao.getTasksToUpload() }
    }
}