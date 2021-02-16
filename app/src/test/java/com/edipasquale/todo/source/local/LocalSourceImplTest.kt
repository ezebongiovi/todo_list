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
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class LocalSourceImplTest {
    private val _testCoroutineDispatcher = TestCoroutineDispatcher()
    private val _testCoroutineScope = TestCoroutineScope(_testCoroutineDispatcher)
    private val _mockedDao: TaskDao = mockk()
    private val _source = LocalSourceImpl(_mockedDao)

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        Dispatchers.setMain(_testCoroutineDispatcher)

        every { _mockedDao.insertTasks(any()) } returns emptyList()
        every { _mockedDao.getAllTasks() } returns emptyList()
        every { _mockedDao.getTasks(any()) } returns emptyList()
        every { _mockedDao.getTasksToUpload() } returns emptyList()
        every { _mockedDao.getTasksLiveData(any()) } returns MutableLiveData<List<TaskEntity>>().apply {
            value = emptyList()
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun createTasks() {
        // Insert a list of tags
        val list = listOf(
            TaskEntity(name = "Task one")
        )
        _source.createTasks(list)

        // Verify the TasksDao is being called with the same list
        verify(exactly = 1) { _mockedDao.insertTasks(list) }
    }

    @Test
    fun createTasksWithEmptyList() {
        // Insert a list of empty tags
        val list = emptyList<TaskEntity>()
        _source.createTasks(list)

        // Verify the TasksDao is not being called since the list was empty
        verify(exactly = 0) { _mockedDao.insertTasks(any()) }
    }

    @Test
    fun getTasks() {
        // Get all undone tasks
        _source.getTasks(false)

        // Verify the TasksDao has been called correctly
        verify(exactly = 1) { _mockedDao.getTasks(false) }
    }

    @Test
    fun getPendingTasks() {
        // Get all done tasks
        _source.getTasks(true)

        // Verify the TasksDao has been called correctly
        verify(exactly = 1) { _mockedDao.getTasks(true) }
    }

    @Test
    fun getTasksLiveData() = _testCoroutineScope.runBlockingTest {
        _source.getTasksLiveData(false).getOrAwaitValue()

        verify(exactly = 1) { _mockedDao.getTasksLiveData(false) }
    }

    @Test
    fun getTasksToUpload() = _testCoroutineScope.runBlockingTest {
        _source.getTasksToUpload()

        verify(exactly = 1) { _mockedDao.getTasksToUpload() }
    }
}