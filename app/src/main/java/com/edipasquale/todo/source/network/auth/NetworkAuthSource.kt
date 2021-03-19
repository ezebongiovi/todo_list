package com.edipasquale.todo.source.network.auth

import com.edipasquale.todo.dto.APIError
import com.edipasquale.todo.dto.APIResult
import com.edipasquale.todo.dto.Credential

interface NetworkAuthSource {

    suspend fun authenticate(apiKey: String, userName: String) : APIResult<Credential, APIError>
}