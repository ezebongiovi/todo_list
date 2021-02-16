package com.edipasquale.todo.dto

sealed class APIResult<out Success, out Failure>
data class Success<out Success>(val value: Success) : APIResult<Success, Nothing>()
data class Failure<out Failure>(val reason: Failure) : APIResult<Nothing, Failure>()