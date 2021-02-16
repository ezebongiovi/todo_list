package com.edipasquale.todo.source.network

import com.apollographql.apollo.api.Mutation
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.Query
import com.edipasquale.todo.dto.APIError
import com.edipasquale.todo.dto.APIResult
import kotlinx.coroutines.flow.Flow

interface GraphQLSource {

    /**
     * Executes a [Query] and maps the response into [APIResult]
     *
     * @param query
     */
    fun <Q : Query<out Operation.Data, T, out Operation.Variables>, T : Any> executeQuery(
        query: Q
    ): Flow<APIResult<T, APIError>>

    /**
     * Executes a [Mutation] and maps the response into [APIResult]
     *
     * @param mutation
     */
    fun <Q : Mutation<out Operation.Data, T, out Operation.Variables>, T : Any> executeMutation(
        mutation: Q
    ): Flow<APIResult<T, APIError>>
}