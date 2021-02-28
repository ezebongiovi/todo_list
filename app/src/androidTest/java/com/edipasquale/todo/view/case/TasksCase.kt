package com.edipasquale.todo.view.case

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.api.Response
import com.edipasquale.todo.KoinTestApp
import com.edipasquale.todo.db.entity.TaskEntity
import com.edipasquale.todo.dto.APIError
import com.edipasquale.todo.dto.APIResult
import com.edipasquale.todo.dto.Success
import com.edipasquale.todo.repository.AuthRepository
import com.edipasquale.todo.repository.TasksRepository
import com.edipasquale.todo.view.page.TasksPage
import com.edipasquale.todo.viewmodel.AuthViewModel
import com.edipasquale.todo.viewmodel.CreateTaskViewModel
import com.edipasquale.todo.viewmodel.TasksViewModel
import com.example.todolist.GenerateAccessTokenMutation
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
class TasksCase {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var _koinApp: KoinTestApp
    private val _mockAuthRepository: AuthRepository = mockk()
    private val _mockRepository : TasksRepository = mockk()

    @Before
    fun setUp() {
        _koinApp = ApplicationProvider.getApplicationContext()

        _koinApp.setUpModule(module {
            viewModel(override = true) { TasksViewModel(androidApplication(), _mockRepository) }
            viewModel(override = true) { AuthViewModel(androidApplication(), _mockAuthRepository) }
            viewModel(override = true) { CreateTaskViewModel(androidApplication(), _mockRepository) }
        })

        every { _mockRepository.getTasks(any()) } returns MutableLiveData<List<TaskEntity>>().apply {
            value =  emptyList()
        } as LiveData<List<TaskEntity>>

        val mockedOperation = mockk<Mutation<Operation.Data, GenerateAccessTokenMutation.Data, Operation.Variables>>()

        every { _mockAuthRepository.authenticate() } returns flowOf<APIResult<Response<GenerateAccessTokenMutation.Data>, APIError>>(
            Success(Response.builder<GenerateAccessTokenMutation.Data>(mockedOperation).build())
        )
    }

    @Test
    fun navigateToCreateTask() {
        TasksPage()
            .launch()
            .moveToState(Lifecycle.State.RESUMED)
            .navigateToCreateTask()
            .checkVisibility()
    }
}