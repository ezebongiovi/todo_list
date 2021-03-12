package com.edipasquale.todo.source.network.tasks.impl

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.coroutines.toFlow
import com.edipasquale.todo.dto.*
import com.edipasquale.todo.source.network.tasks.NetworkTasksSource
import kotlinx.coroutines.flow.first

abstract class GraphQLSource(private val _apolloClient: ApolloClient) : NetworkTasksSource {

    /**
     * Executes a [Query] and maps the response into an [APIResult]
     *
     * @param query the query being executed
     */
    protected suspend fun <Q : Query<out Operation.Data, T, out Operation.Variables>, T : Any> executeQuery(
        query: Q
    ): APIResult<T, APIError> {
        val response = _apolloClient.query(query).toFlow().first()

        return handleResponse(response)
    }

    /**
     * Executes a [Mutation] and maps the response into an [APIResult]
     *
     * @param mutation the mutation being executed
     */
    protected suspend fun <Q : Mutation<out Operation.Data, T, out Operation.Variables>, T : Any> executeMutation(
        mutation: Q
    ): APIResult<T, APIError> {
        val response = _apolloClient.mutate(mutation).toFlow().first()

        return handleResponse(response)
    }

    /**
     * Handles error responses and invalid data on response
     *
     * @param response the response being handled
     */
    private fun <T> handleResponse(response: Response<T>): APIResult<T, APIError> {
        val topLevelError = getErrorsFromResponse(response)

        return if (topLevelError == null)
            getDataFromResponse(response)
        else
            return topLevelError
    }

    private fun <T> getErrorsFromResponse(response: Response<T>): Failure<APIError>? {
        return response.errors?.let { errors ->
            val topLevelError = errors.first()

            Failure(APIError.fromApolloError(topLevelError))
        }
    }

    private fun <T> getDataFromResponse(response: Response<T>): APIResult<T, APIError> {
        val data = response.data

        return if (data == null)
            Failure(APIError(ERROR_INVALID_DATA))
        else
            Success(data)
    }
}