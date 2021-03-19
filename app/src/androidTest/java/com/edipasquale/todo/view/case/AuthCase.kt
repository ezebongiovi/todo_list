package com.edipasquale.todo.view.case

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import com.apollographql.apollo.exception.ApolloNetworkException
import com.edipasquale.todo.KoinTestApp
import com.edipasquale.todo.dto.APIError
import com.edipasquale.todo.dto.Credential
import com.edipasquale.todo.dto.Failure
import com.edipasquale.todo.dto.Success
import com.edipasquale.todo.repository.AuthRepository
import com.edipasquale.todo.repository.TasksRepository
import com.edipasquale.todo.view.fragment.TasksFragment
import com.edipasquale.todo.view.page.TasksPage
import com.edipasquale.todo.viewmodel.AuthViewModel
import com.edipasquale.todo.viewmodel.TasksViewModel
import io.mockk.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class AuthCase {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var _koinApp: KoinTestApp
    private val _mockedAuthRepository: AuthRepository = mockk()
    private val _mockedRepository: TasksRepository = mockk()

    @Before
    fun setUp() {
        _koinApp = ApplicationProvider.getApplicationContext()

        _koinApp.setUpModule(module {
            viewModel(override = true) {
                AuthViewModel(
                    androidApplication(),
                    _mockedAuthRepository
                )
            }
            viewModel(override = true) { TasksViewModel(androidApplication(), _mockedRepository) }
        })

        every { _mockedRepository.getTasks() } returns MutableLiveData(emptyList())
    }

    @Test
    fun testAuthSuccess() {
        // Preparation
        val mockedFragment = spyk<TasksFragment>()
        coEvery { _mockedAuthRepository.authenticate() } coAnswers { Success(Credential("Some token")) }

        // Execution
        TasksPage()
            .launchWithFragment(mockedFragment)
            .moveToState(Lifecycle.State.RESUMED)
            .onFragment() {
                // Verification
                verify(exactly = 1) { mockedFragment.onAuthenticated(any()) }
                verify(exactly = 0) { mockedFragment.onAuthenticationError(any()) }
            }
    }

    @Test
    fun testAuthNetworkError() {
        // Preparation
        val mockedFragment = spyk<TasksFragment>()
        coEvery { _mockedAuthRepository.authenticate() } coAnswers {
            Failure(APIError.fromException(ApolloNetworkException("")))
        }

        // Execution
        TasksPage()
            .launchWithFragment(mockedFragment)
            .moveToState(Lifecycle.State.RESUMED)
            .onFragment() {
                // Verification
                verify(exactly = 1) { mockedFragment.onAuthenticationError(any()) }
                verify(exactly = 0) { mockedFragment.onAuthenticated(any()) }
            }
    }

    @Test
    fun testAuthGenericError() {
        // Preparation
        val mockedFragment = spyk<TasksFragment>()
        coEvery { _mockedAuthRepository.authenticate() } coAnswers {
            Failure(APIError(""))
        }

        // Execution
        TasksPage()
            .launchWithFragment(mockedFragment)
            .moveToState(Lifecycle.State.RESUMED)
            .onFragment() {
                // Verification
                verify(exactly = 1) { mockedFragment.onAuthenticationError(any()) }
                verify(exactly = 0) { mockedFragment.onAuthenticated(any()) }
            }
    }
}