package com.edipasquale.todo.db.entity

import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TaskEntityTest {

    @Test
    fun isSynced() {
        Assert.assertFalse(
            TaskEntity(
                name = "Some name"
            ).isSynced()
        )

        Assert.assertTrue(
            TaskEntity(
                id = "Some ID",
                name = "Some name"
            ).isSynced()
        )
    }

    @Test
    fun setId() {
        val id = 0L
        assertEquals(
            id, TaskEntity(
                name = "Some name"
            ).apply {
                _id = id
            }._id
        )
    }
}