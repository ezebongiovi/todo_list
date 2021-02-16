package com.edipasquale.todo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.api.Response
import com.edipasquale.todo.R
import com.edipasquale.todo.dto.*
import com.edipasquale.todo.repository.AuthRepository
import com.example.todolist.GenerateAccessTokenMutation
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AuthViewModel(
    app: Application,
    private val _repository: AuthRepository
) : AndroidViewModel(app) {
    private val _authModel = MutableLiveData<String>()
    private val _errorModel = MutableLiveData<String>()
    val errorModel: LiveData<String> = _errorModel
    val authModel: LiveData<String> = _authModel

    fun authenticate() = viewModelScope.launch {
        _repository.authenticate().collect { response ->
            handleResponse(response)
        }
    }

    private fun handleResponse(response: APIResult<Response<GenerateAccessTokenMutation.Data>, APIError>) {
        when (response) {
            is Success -> {
                // Handle errors
                val errors = response.value.errors
                if (errors?.isNotEmpty() == true) {
                    _errorModel.postValue(errors.first().message)
                }

                // Handle data
                response.value.data?.generateAccessToken?.let { token ->
                    _authModel.postValue(token)
                }
                    ?: _errorModel.postValue(getApplication<Application>().getString(R.string.error_auth))
            }

            is Failure -> {
                if (response.reason.error == ERROR_NETWORK)
                    _errorModel.postValue(getApplication<Application>().getString(R.string.error_network))
                else
                    _errorModel.postValue(response.reason.error)
            }
        }
    }
}