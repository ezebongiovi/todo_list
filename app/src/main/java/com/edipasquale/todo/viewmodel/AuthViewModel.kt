package com.edipasquale.todo.viewmodel

import android.app.Application
import android.content.Context
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

    fun authenticate() = viewModelScope.launch(Dispatchers.IO) {
        val response = _repository.authenticate()

        handleResponse(response)
    }

    private fun handleResponse(response: APIResult<String, APIError>) {
        val context = getApplication<Application>()

        when (response) {
            is Success -> _authModel.postValue(response.value!!)
            is Failure -> _errorModel.postValue(getFailureMessage(context, response))
        }
    }

    private fun getFailureMessage(context: Context, response: Failure<APIError>): String {
        return if (response.reason.error == ERROR_NETWORK)
            context.getString(R.string.error_network)
        else
            context.getString(R.string.error_auth)
    }
}