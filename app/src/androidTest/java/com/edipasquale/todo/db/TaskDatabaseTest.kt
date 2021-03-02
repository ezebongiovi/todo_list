package com.edipasquale.todo.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.edipasquale.todo.db.entity.TaskEntity
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskDatabaseTest {
    private lateinit var _tasksDao: TaskDao
    private lateinit var _dao: TaskDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        _dao = Room.inMemoryDatabaseBuilder(
            context, TaskDatabase::class.java
        ).build()
        _tasksDao = _dao.tasksDao()
    }

    @After
    fun shutDown() {
        _dao.close()
    }

    @Test
    fun createTasks() {
        val entity = TaskEntity(name = "Some Task")
        val insertedTask = _tasksDao.createTask(entity)
        val allTasks = _tasksDao.getAllTasks()

        Assert.assertEquals(1, allTasks.size)
        Assert.assertEquals(insertedTask, allTasks.first())
    }

    @Test
    fun getTasks() {
        val allTasks = _tasksDao.getAllTasks()

        Assert.assertTrue(allTasks.isEmpty())

        val insertedTask = _tasksDao.createTask(TaskEntity(name = "Some other task"))
        val allTasksAfterInsertion = _tasksDao.getAllTasks()

        Assert.assertEquals(1, allTasksAfterInsertion.size)
        Assert.assertEquals(insertedTask, allTasksAfterInsertion[0])
    }

    @Test
    fun getPendingTasks() {
        val allTasks = _tasksDao.getAllTasks()
        Assert.assertTrue(allTasks.isEmpty())

        val insertedDoneTask = _tasksDao.createTask(TaskEntity(name = "Done", isDone = true))
        val insertedUnDoneTask = _tasksDao.createTask(TaskEntity(name = "Undone", isDone = false))
        val doneTasks = _tasksDao.getTasks(true)
        val unDoneTasks = _tasksDao.getTasks(false)

        Assert.assertEquals(1, doneTasks.size)
        Assert.assertEquals(insertedDoneTask, doneTasks.first())
        Assert.assertEquals(1, unDoneTasks.size)
        Assert.assertEquals(insertedUnDoneTask, unDoneTasks.first())
    }

    @Test
    fun getNotSyncedTasks() {
        val allTasks = _tasksDao.getAllTasks()

        Assert.assertTrue(allTasks.isEmpty())

        val insertedTask = _tasksDao.createTask(TaskEntity(name = "Some task", isDone = true))
        val notSyncedTasks = _tasksDao.getTasksToUpload()

        Assert.assertEquals(1, notSyncedTasks.size)
        Assert.assertEquals(insertedTask, notSyncedTasks.first())
    }
}