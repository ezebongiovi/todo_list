package com.edipasquale.todo.source.network

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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map

@ExperimentalCoroutinesApi
open class GraphQLSourceImpl(private val apolloClient: ApolloClient) : GraphQLSource {

    override fun <Q : Query<out Operation.Data, T, out Operation.Variables>, T : Any> executeQuery(
        query: Q
    ) = apolloClient.query(query)
        .toFlow()
        .map {
            handleResponse(it)
        }

    override fun <Q : Mutation<out Operation.Data, T, out Operation.Variables>, T : Any> executeMutation(
        mutation: Q
    ) = apolloClient.mutate(mutation)
        .toFlow()
        .map {
            handleResponse(it)
        }

    /**
     * Applies business logic to the response. Handling error responses and invalid data
     *
     * @param response the response for which to apply the business logic
     */
    protected open fun <T> handleResponse(
        response: Response<T>
    ): APIResult<T, APIError> {

        // If there are errors on the response we propagate the top level error
        response.errors?.let { errors ->
            val topLevelError = errors.first()

            return Failure(APIError.fromApolloError(topLevelError))
        }

        // If data is null we propagate a custom error
        val data = response.data
            ?: return Failure(APIError.fromException(ApolloException("Data is null")))

        return Success(data)
    }
}