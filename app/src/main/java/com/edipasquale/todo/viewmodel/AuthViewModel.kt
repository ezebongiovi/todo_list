package com.edipasquale.todo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.edipasquale.todo.R
import com.edipasquale.todo.dto.*
import com.edipasquale.todo.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthViewModel(
    app: Application,
    private val _repository: AuthRepository
) : AndroidViewModel(app) {
    private val _authModel = MutableLiveData<String>()
    private val _errorModel = MutableLiveData<String>()
    val errorModel: LiveData<String> = _errorModel
    val authModel: LiveData<String> = _authModel

    fun authenticate() {
        viewModelScope.launch(Dispatchers.IO) {
            val response = _repository.authenticate()

            handleResponse(response)
        }
    }

    private fun handleResponse(response: APIResult<Credential, APIError>) {
        when (response) {
            is Success -> handleSuccess(response.value)
            is Failure -> handleError(response.reason)
        }
    }

    private fun handleSuccess(credential: Credential) {
        _authModel.postValue(credential.token)
    }

    private fun handleError(error: APIError) {
        val context = getApplication<Application>()
        val errorMessage = if (error.error == ERROR_NETWORK)
            context.getString(R.string.error_network)
        else
            context.getString(R.string.error_auth)

        _errorModel.postValue(errorMessage)
    }
}