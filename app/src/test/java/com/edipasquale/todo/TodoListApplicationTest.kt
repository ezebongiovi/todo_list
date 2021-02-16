package com.edipasquale.todo

import com.edipasquale.todo.koin.AppInjector
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class TodoListApplicationTest {

    @Before
    fun setUp() {
        mockkObject(AppInjector)
        mockkObject(AppInjector.Companion)
    }

    @Test
    fun onCreate() {
        every { AppInjector.Companion.start(any()) } returns Unit

        val app = TodoListApplication()
        app.onCreate()


        verify(exactly = 1) { AppInjector.start(app) }
    }
}