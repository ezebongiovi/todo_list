package com.edipasquale.todo.source.network

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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class GraphQLSourceImplTest {

    private val _mockedClient = mockk<ApolloClient>()

    @Before
    fun setUp() {
        mockkStatic("com.apollographql.apollo.coroutines.CoroutinesExtensionsKt")
    }

    @Test
    fun `QUERY response null data null errors case`() = runBlocking {
        // Object under test
        val source = GraphQLSourceImpl(_mockedClient)

        // Mock the operation we execute during tests
        val mockedOperation = mockk<Query<Operation.Data, String, Operation.Variables>>()

        mockQueryResponse(mockedOperation, Response.builder<String>(mockedOperation).build())

        // Make the call
        source.executeQuery(mockedOperation).collect { response ->
            // Assert its an error
            assertTrue(response is Failure)
            assertEquals(ERROR_UNKNOWN, (response as Failure).reason.error)
        }
    }

    @Test
    fun `QUERY Response null data with top level error case `() = runBlocking {
        // Object under test
        val source = GraphQLSourceImpl(_mockedClient)

        // Mock the operation we execute during tests
        val mockedOperation = mockk<Query<Operation.Data, String, Operation.Variables>>()
        val expectedError = Error("Some error")

        mockQueryResponse(mockedOperation, Response.builder<String>(mockedOperation)
            .errors(listOf(
                expectedError
            ))
            .build())

        // Make the call
        source.executeQuery(mockedOperation).collect { response ->
            // Assert its an error
            assertTrue(response is Failure)

            assertEquals(ERROR_GRAPHQL, (response as Failure).reason.error)
            assertEquals(expectedError.message, response.reason.errorDescription)
        }
    }

    @Test
    fun `MUTATION response null data null errors case`() = runBlocking {
        // Object under test
        val source = GraphQLSourceImpl(_mockedClient)

        // Mock the operation we execute during tests
        val mockedOperation = mockk<Mutation<Operation.Data, String, Operation.Variables>>()

        mockMutationResponse(mockedOperation, Response.builder<String>(mockedOperation).build())

        // Make the call
        source.executeMutation(mockedOperation).collect { response ->
            // Assert its an error
            assertTrue(response is Failure)

            assertEquals(ERROR_UNKNOWN, (response as Failure).reason.error)
        }
    }

    @Test
    fun `MUTATION Response null data with top level error case`() = runBlocking {
        // Object under test
        val source = GraphQLSourceImpl(_mockedClient)

        // Mock the operation we execute during tests
        val mockedOperation = mockk<Mutation<Operation.Data, String, Operation.Variables>>()
        val expectedError = Error("Some error")

        mockMutationResponse(mockedOperation, Response.builder<String>(mockedOperation)
            .errors(listOf(
                expectedError
            ))
            .build())

        // Make the call
        source.executeMutation(mockedOperation).collect { response ->
            // Assert its an error
            assertTrue(response is Failure)

            assertEquals(ERROR_GRAPHQL, (response as Failure).reason.error)
            assertEquals(expectedError.message, response.reason.errorDescription)
        }
    }

    @Test
    fun `MUTATION Success case`() = runBlocking {
        // Object under test
        val source = GraphQLSourceImpl(_mockedClient)

        // Mock the operation we execute during tests
        val mockedOperation = mockk<Mutation<Operation.Data, String, Operation.Variables>>()
        val expectedResult = "Some response"

        mockMutationResponse(mockedOperation, Response.builder<String>(mockedOperation)
            .data(expectedResult)
            .build())

        // Make the call
        source.executeMutation(mockedOperation).collect { response ->
            // Assert its an error
            assertTrue(response is Success)

            assertEquals(expectedResult, (response as Success).value)
        }
    }

    @Test
    fun `QUERY Success case`() = runBlocking {
        // Object under test
        val source = GraphQLSourceImpl(_mockedClient)

        // Mock the operation we execute during tests
        val mockedOperation = mockk<Query<Operation.Data, String, Operation.Variables>>()
        val expectedResult = "Some response"

        mockQueryResponse(mockedOperation, Response.builder<String>(mockedOperation)
            .data(expectedResult)
            .build())

        // Make the call
        source.executeQuery(mockedOperation).collect { response ->
            // Assert its an error
            assertTrue(response is Success)

            assertEquals(expectedResult, (response as Success).value)
        }
    }

    private fun <T> mockQueryResponse(query: Query<Operation.Data, T, Operation.Variables>, response: Response<T>) {
        // Mock the ApolloQueryCall result when calling [ApolloClient.query]
        val mockedCallback = spyk<ApolloQueryCall<T>>()

        // Stub responses from mock source
        every { _mockedClient.query(query) } returns mockedCallback

        // Stub callback result to a response without errors or data
        every { mockedCallback.toFlow() } returns flowOf(response)
    }

    private fun <T> mockMutationResponse(mutation: Mutation<Operation.Data, T, Operation.Variables>, response: Response<T>) {
        // Mock the ApolloQueryCall result when calling [ApolloClient.query]
        val mockedCallback = spyk<ApolloMutationCall<T>>()

        // Stub responses from mock source
        every { _mockedClient.mutate(mutation) } returns mockedCallback

        // Stub callback result to a response without errors or data
        every { mockedCallback.toFlow() } returns flowOf(response)
    }
}