package com.edipasquale.todo.source.network

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.coroutines.toFlow
import com.apollographql.apollo.exception.ApolloException
import com.edipasquale.todo.dto.APIError
import com.edipasquale.todo.dto.APIResult
import com.edipasquale.todo.dto.Failure
import com.edipasquale.todo.dto.Success
import kotlinx.coroutines.flow.first
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

open class GraphQLSourceImpl(private val _apolloClient: ApolloClient) : GraphQLSource {

    override suspend fun <Q : Query<out Operation.Data, T, out Operation.Variables>, T : Any> executeQuery(
        query: Q
    ) = suspendCoroutine<APIResult<T, APIError>> { continuation ->
        _apolloClient.query(query).enqueue(getCallback<T>(continuation))
    }

    override suspend fun <Q : Mutation<out Operation.Data, T, out Operation.Variables>, T : Any> executeMutation(
        mutation: Q
    ): APIResult<T, APIError> {
        val response = _apolloClient.mutate(mutation).toFlow().first()

        return handleResponse(response)
    }

    /**
     * Handles error responses and invalid data
     *
     * @param response
     */
    protected open fun <T> handleResponse(response: Response<T>): APIResult<T, APIError> {
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
            Failure(APIError.fromException(ApolloException("Data is null")))
        else
            Success(data)
    }

    private fun <T : Any> getCallback(continuation: Continuation<APIResult<T, APIError>>) =
        object : ApolloCall.Callback<T>() {
            override fun onResponse(response: Response<T>) {
                continuation.resume(handleResponse(response))
            }

            override fun onFailure(e: ApolloException) {
                continuation.resume(Failure(APIError.fromException(e)))
            }
        }
}
