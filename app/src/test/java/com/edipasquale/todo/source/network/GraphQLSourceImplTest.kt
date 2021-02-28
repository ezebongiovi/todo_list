package com.edipasquale.todo.source.network

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.ApolloMutationCall
import com.apollographql.apollo.ApolloQueryCall
import com.apollographql.apollo.api.*
import com.apollographql.apollo.coroutines.toFlow
import com.edipasquale.todo.dto.ERROR_GRAPHQL
import com.edipasquale.todo.dto.ERROR_UNKNOWN
import com.edipasquale.todo.dto.Failure
import com.edipasquale.todo.dto.Success
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
        val mockedOperation = mockk<Query<Operation.Data, String, Operation.Variables>>()

        mockQueryResponse(mockedOperation, Response.builder<String>(mockedOperation).build())

        val response = objectUnderTest.executeQuery(mockedOperation)
        assertTrue(response is Failure)
        assertEquals(ERROR_UNKNOWN, (response as Failure).reason.error)
    }

    @Test
    fun `QUERY Response null data with top level error case`() = runBlocking {
        val objectUnderTest = GraphQLSourceImpl(_mockedClient)
        val mockedOperation = mockk<Query<Operation.Data, String, Operation.Variables>>()
        val expectedError = Error("Some error")

        mockQueryError(mockedOperation, expectedError)

        val response = objectUnderTest.executeQuery(mockedOperation)
        assertTrue(response is Failure)
        assertEquals(ERROR_GRAPHQL, (response as Failure).reason.error)
        assertEquals(expectedError.message, response.reason.errorDescription)
    }

    @Test
    fun `MUTATION response null data null errors case`() = runBlocking {
        val objectUnderTest = GraphQLSourceImpl(_mockedClient)
        val mockedOperation = mockk<Mutation<Operation.Data, String, Operation.Variables>>()

        mockMutationResponse(mockedOperation, Response.builder<String>(mockedOperation).build())

        val response = objectUnderTest.executeMutation(mockedOperation)
        assertTrue(response is Failure)
        assertEquals(ERROR_UNKNOWN, (response as Failure).reason.error)
    }

    @Test
    fun `MUTATION Response null data with top level error case`() = runBlocking {
        val objectUnderTest = GraphQLSourceImpl(_mockedClient)
        val mockedOperation = mockk<Mutation<Operation.Data, String, Operation.Variables>>()
        val expectedError = Error("Some error")

        mockMutationError(mockedOperation, expectedError)

        val response = objectUnderTest.executeMutation(mockedOperation)
        assertTrue(response is Failure)
        assertEquals(ERROR_GRAPHQL, (response as Failure).reason.error)
        assertEquals(expectedError.message, response.reason.errorDescription)
    }

    @Test
    fun `MUTATION Success case`() = runBlocking {
        val objectUnderTest = GraphQLSourceImpl(_mockedClient)
        val mockedOperation = mockk<Mutation<Operation.Data, String, Operation.Variables>>()
        val expectedResult = "Success response"

        mockMutationResponse(
            mockedOperation,
            Response.builder<String>(mockedOperation).data(expectedResult).build()
        )

        val response = objectUnderTest.executeMutation(mockedOperation)
        assertTrue(response is Success)
        assertEquals(expectedResult, (response as Success).value)
    }

    @Test
    fun `QUERY Success case`() = runBlocking {
        val objectUnderTest = GraphQLSourceImpl(_mockedClient)
        val mockedOperation = mockk<Query<Operation.Data, String, Operation.Variables>>()
        val expectedResult = "Some response"

        mockQueryResponse(
            mockedOperation, Response.builder<String>(mockedOperation)
                .data(expectedResult)
                .build()
        )

        val response = objectUnderTest.executeQuery(mockedOperation)
        assertTrue(response is Success)
        assertEquals(expectedResult, (response as Success).value)
    }

    private fun <T> mockQueryResponse(
        query: Query<Operation.Data, T, Operation.Variables>,
        response: Response<T>
    ) {
        // Mock the ApolloQueryCall result when calling [ApolloClient.query]
        val mockedCallback = spyk<ApolloQueryCall<T>>()

        every { _mockedClient.query(query) } returns mockedCallback
        every { mockedCallback.toFlow() } answers { flowOf(response) }
    }

    private fun <T> mockMutationResponse(
        mutation: Mutation<Operation.Data, T, Operation.Variables>,
        response: Response<T>
    ) {
        val mockedCallback = spyk<ApolloMutationCall<T>>()

        every { _mockedClient.mutate(mutation) } returns mockedCallback
        every { mockedCallback.toFlow() } answers { flowOf(response) }
    }

    private fun mockMutationError(
        mockedOperation: Mutation<Operation.Data, String, Operation.Variables>,
        expectedError: Error
    ) {
        val errors = listOf(expectedError)

        mockMutationResponse(
            mockedOperation,
            Response.builder<String>(mockedOperation).errors(errors).build()
        )
    }

    private fun mockQueryError(
        mockedOperation: Query<Operation.Data, String, Operation.Variables>,
        expectedError: Error
    ) {
        val errors = listOf(expectedError)

        mockQueryResponse(
            mockedOperation,
            Response.builder<String>(mockedOperation).errors(errors).build()
        )
    }
}