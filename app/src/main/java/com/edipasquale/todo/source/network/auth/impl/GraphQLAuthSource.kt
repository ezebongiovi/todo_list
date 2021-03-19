package com.edipasquale.todo.source.network.auth.impl

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.toFlow
import com.edipasquale.todo.dto.*
import com.edipasquale.todo.source.network.auth.NetworkAuthSource
import com.example.todolist.GenerateAccessTokenMutation
import kotlinx.coroutines.flow.first

class GraphQLAuthSource(private val _apolloClient: ApolloClient) : NetworkAuthSource {

    override suspend fun authenticate(apiKey: String, userName: String): APIResult<Credential, APIError> {
        return try {
            val mutation = GenerateAccessTokenMutation(apiKey, userName)
            val response = _apolloClient.mutate(mutation).toFlow().first()

            response.data?.generateAccessToken?.let { userToken ->
                Success(Credential(userToken))
            } ?: Failure(APIError(ERROR_INVALID_DATA))

        } catch (e: Exception) {
            Failure(APIError.fromException(e))
        }
    }
}