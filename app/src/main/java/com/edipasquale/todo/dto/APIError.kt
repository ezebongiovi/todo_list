package com.edipasquale.todo.dto

import com.apollographql.apollo.api.Error
import com.apollographql.apollo.exception.ApolloNetworkException

const val ERROR_UNKNOWN = "unknown_error"
const val ERROR_GRAPHQL = "graphql_error"
const val ERROR_NETWORK = "error_network"

class APIError(
    var error: String,
    var errorDescription: String? = null,
) {
    companion object {

        fun fromApolloError(error: Error): APIError {
            return APIError(ERROR_GRAPHQL, error.message)
        }

        fun fromApolloErrorMessage(message: String): APIError {
            return APIError(ERROR_GRAPHQL, message)
        }

        fun fromException(exception: Exception): APIError {
            return if (exception is ApolloNetworkException)
                APIError(ERROR_NETWORK)
            else
                APIError(ERROR_UNKNOWN, exception.message ?: "")
        }
    }
}