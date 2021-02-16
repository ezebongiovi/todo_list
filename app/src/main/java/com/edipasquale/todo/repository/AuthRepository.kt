package com.edipasquale.todo.repository

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.coroutines.await
import com.edipasquale.todo.BuildConfig
import com.edipasquale.todo.dto.APIError
import com.edipasquale.todo.dto.APIResult
import com.edipasquale.todo.dto.Failure
import com.edipasquale.todo.dto.Success
import com.example.todolist.GenerateAccessTokenMutation
import kotlinx.coroutines.flow.flow

class AuthRepository(
    private val _apolloClient: ApolloClient
) {

    fun authenticate() = flow<APIResult<Response<GenerateAccessTokenMutation.Data>, APIError>>{
        try {
            val response = _apolloClient.mutate(
                GenerateAccessTokenMutation(
                    apiKey = BuildConfig.API_KEY,
                    userName = BuildConfig.USER_NAME
                )
            ).await()

            emit(Success(response))

        } catch (e: Exception) {
            emit(Failure(APIError.fromException(e)))
        }
    }
}