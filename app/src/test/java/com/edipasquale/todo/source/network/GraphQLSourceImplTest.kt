package com.edipasquale.todo.source.network

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.ApolloMutationCall
import com.apollographql.apollo.ApolloQueryCall
import com.apollographql.apollo.api.*
import com.apollographql.apollo.coroutines.toFlow
import com.edipasquale.todo.db.entity.TaskEntity
import com.edipasquale.todo.dto.ERROR_GRAPHQL
import com.edipasquale.todo.dto.ERROR_INVALID_DATA
import com.edipasquale.todo.dto.Failure
import com.edipasquale.todo.dto.Success
import com.example.todolist.CreateTaskMutation
import com.example.todolist.GetAllTasksQuery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class GraphQLSourceImplTest {

    private val _mockedClient = mockk<ApolloClient>()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        mockkStatic("com.apollographql.apollo.coroutines.CoroutinesExtensionsKt")
    }

    @Test
    fun `QUERY response null data null errors case`() = runBlocking {
        val objectUnderTest = GraphQLSourceImpl(_mockedClient)

        mockQueryResponse(Response.builder<String>(mockk()).build())

        val response = objectUnderTest.getAllTasks()
        assertTrue(response is Failure)
        assertEquals(ERROR_INVALID_DATA, (response as Failure).reason.error)
    }

    @Test
    fun `QUERY Response null data with top level error case`() = runBlocking {
        val objectUnderTest = GraphQLSourceImpl(_mockedClient)
        val mockedOperation = mockk<Query<Operation.Data, String, Operation.Variables>>()
        val expectedError = Error("Some error")

        mockQueryError(mockedOperation, expectedError)

        val response = objectUnderTest.getAllTasks()
        assertTrue(response is Failure)
        assertEquals(ERROR_GRAPHQL, (response as Failure).reason.error)
        assertEquals(expectedError.message, response.reason.errorDescription)
    }

    @Test
    fun `MUTATION response null data null errors case`() = runBlocking {
        val objectUnderTest = GraphQLSourceImpl(_mockedClient)
        val mockedOperation = mockk<Mutation<Operation.Data, String, Operation.Variables>>()
        val entity = TaskEntity(name = "Test")

        mockMutationResponse(Response.builder<String>(mockedOperation).build())

        val response = objectUnderTest.createTask(entity)
        assertTrue(response is Failure)
        assertEquals(ERROR_INVALID_DATA, (response as Failure).reason.error)
    }

    @Test
    fun `MUTATION Response null data with top level error case`() = runBlocking {
        val objectUnderTest = GraphQLSourceImpl(_mockedClient)
        val expectedError = Error("Some error")
        val mockedOperation = mockk<Mutation<Operation.Data, String, Operation.Variables>>()
        val entity = TaskEntity(name = "Test", note = "Test")

        mockMutationError(mockedOperation, expectedError)

        val response = objectUnderTest.createTask(entity)
        assertTrue(response is Failure)
        assertEquals(ERROR_GRAPHQL, (response as Failure).reason.error)
        assertEquals(expectedError.message, response.reason.errorDescription)
    }

    @Test
    fun `MUTATION Success case`() = runBlocking {
        val objectUnderTest = GraphQLSourceImpl(_mockedClient)
        val mockedOperation = mockk<Mutation<Operation.Data, Any, Operation.Variables>>()
        val entity = TaskEntity(name = "Test")
        val expectedResult = CreateTaskMutation.Data(
            createTask = CreateTaskMutation.CreateTask(
                id = "some",
                name = entity.name,
                note = entity.note,
                isDone = entity.isDone
            )
        )

        mockMutationResponse(Response.builder<Any>(mockedOperation).data(expectedResult).build())

        val response = objectUnderTest.createTask(entity)
        assertTrue(response is Success)
        assertEquals(entity.copy(id = "some"), (response as Success).value)
    }

    @Test
    fun `QUERY Success case`() = runBlocking {
        val objectUnderTest = GraphQLSourceImpl(_mockedClient)
        val mockedOperation = mockk<Query<Operation.Data, Any, Operation.Variables>>()
        val expectedResult = GetAllTasksQuery.Data(
            allTasks = emptyList()
        )

        mockQueryResponse(
            Response.builder<Any>(mockedOperation)
                .data(expectedResult)
                .build()
        )

        val response = objectUnderTest.getAllTasks()
        assertTrue(response is Success)
        assertEquals(emptyList<Any>(), (response as Success).value)
    }

    private fun <T> mockQueryResponse(
        response: Response<T>
    ) {
        val mockedCallback = spyk<ApolloQueryCall<T>>()

        every { _mockedClient.query(any<Query<out Operation.Data, T, out Operation.Variables>>()) } returns mockedCallback
        every { mockedCallback.toFlow() } answers { flowOf(response) }
    }

    private fun <T> mockMutationResponse(response: Response<T>) {
        val mockedCallback = spyk<ApolloMutationCall<T>>()

        every { _mockedClient.mutate(any<Mutation<out Operation.Data, T, out Operation.Variables>>()) } returns mockedCallback
        every { mockedCallback.toFlow() } answers { flowOf(response) }
    }

    private fun <M : Mutation<out Operation.Data, T, out Operation.Variables>, T : Any> mockMutationError(
        mockedOperation: M,
        expectedError: Error
    ) {
        val errors = listOf(expectedError)

        mockMutationResponse(Response.builder<T>(mockedOperation).errors(errors).build())
    }

    private fun <Q : Query<out Operation.Data, T, out Operation.Variables>, T : Any> mockQueryError(
        mockedOperation: Q,
        expectedError: Error
    ) {
        val errors = listOf(expectedError)

        mockQueryResponse(Response.builder<T>(mockedOperation).errors(errors).build())
    }
}