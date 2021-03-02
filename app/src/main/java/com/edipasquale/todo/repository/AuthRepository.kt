package com.edipasquale.todo.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.coroutines.toFlow
import com.edipasquale.todo.BuildConfig
import com.edipasquale.todo.dto.*
import com.example.todolist.GenerateAccessTokenMutation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AuthRepository(
    private val _apolloClient: ApolloClient
) {

    suspend fun authenticate(): APIResult<String, APIError> {
        return try {
            val mutation = GenerateAccessTokenMutation(BuildConfig.API_KEY, BuildConfig.USER_NAME)
            val response = _apolloClient.mutate(mutation).toFlow().first()

            response.data?.generateAccessToken?.let { token ->
                Success(token)
            } ?: Failure(APIError(ERROR_INVALID_DATA))


        } catch (e: Exception) {
            Failure(APIError.fromException(e))
        }
    }
}