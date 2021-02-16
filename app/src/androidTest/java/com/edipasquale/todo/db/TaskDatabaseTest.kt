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
        // Insert a new task into the DB
        val entity = listOf(TaskEntity(name = "Some Task"))

        // When inserted the list gets updated with an internal ID.
        val updatedList = _tasksDao.insertTasks(entity)

        // Gets all the tasks from DB
        val allTasks = _tasksDao.getAllTasks()

        // Assert the list only has 1 task
        Assert.assertEquals(1, allTasks.size)

        // Assert the item exists on the DB is the same we inserted
        Assert.assertEquals(updatedList.first(), allTasks.first())
    }

    @Test
    fun getTasks() {
        // At this point the list of tasks must be empty
        val allTasks = _tasksDao.getAllTasks()
        Assert.assertTrue(allTasks.isEmpty())

        // Insert a new task into the DB
        val insertedTasks = _tasksDao.insertTasks(listOf(
            TaskEntity(name = "Some task"),
            TaskEntity(name = "Some other task")
        ))

        // Gets all the tasks from DB
        val allTasks2 = _tasksDao.getAllTasks()

        // Assert the list only has 2 tasks
        Assert.assertEquals(2, allTasks2.size)

        // Assert the items exist on the DB are the same we just inserted
        Assert.assertTrue(allTasks2.containsAll(insertedTasks))
    }

    @Test
    fun getPendingTasks() {
        // At this point the list of tasks must be empty
        val allTasks = _tasksDao.getAllTasks()
        Assert.assertTrue(allTasks.isEmpty())

        // Insert a new task into the DB
        val insertedTasks = _tasksDao.insertTasks(listOf(
            TaskEntity(name = "Some task", isDone = true),
            TaskEntity(name = "Some other task")
        ))

        // Gets done tasks from DB
        val doneTasks = _tasksDao.getTasks(true)
        val unDoneTasks = _tasksDao.getTasks(false)

        // Assert the list of done tags has only 1 task
        Assert.assertEquals(1, doneTasks.size)
        // Assert the done task retrieved from DB matches the one we just inserted
        Assert.assertEquals(insertedTasks[0], doneTasks.first())

        // Assert the list of undone tags has only 1 task
        Assert.assertEquals(1, unDoneTasks.size)
        // Assert the undone task retrieved from DB matches the one we just inserted
        Assert.assertEquals(insertedTasks[1], unDoneTasks.first())
    }

    @Test
    fun getNotSyncedTasks() {
        // At this point the list of tasks must be empty
        val allTasks = _tasksDao.getAllTasks()
        Assert.assertTrue(allTasks.isEmpty())

        // Insert a new task into the DB
        val insertedTasks = _tasksDao.insertTasks(listOf(
            TaskEntity(name = "Some task", isDone = true),
            TaskEntity(id = "some_id", name = "Some other task")
        ))

        // Gets tasks that are not synced from DB
        val notSyncedTasks = _tasksDao.getTasksToUpload()

        // Assert the list of done tags has only 1 task
        Assert.assertEquals(1, notSyncedTasks.size)
        // Assert the done task retrieved from DB matches the one we just inserted
        Assert.assertEquals(insertedTasks[0], notSyncedTasks.first())
    }
}