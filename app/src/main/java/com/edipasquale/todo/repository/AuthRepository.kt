package com.edipasquale.todo.repository

import com.edipasquale.todo.BuildConfig
import com.edipasquale.todo.dto.*
import com.edipasquale.todo.source.network.auth.NetworkAuthSource

class AuthRepository(private val _authSource: NetworkAuthSource) {

    suspend fun authenticate(): APIResult<Credential, APIError> {
        return _authSource.authenticate(BuildConfig.API_KEY, BuildConfig.USER_NAME)
    }
}