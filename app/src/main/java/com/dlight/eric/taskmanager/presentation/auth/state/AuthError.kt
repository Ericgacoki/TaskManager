package com.dlight.eric.taskmanager.presentation.auth.state

sealed class AuthError(val message: String) {

    data object None : AuthError("")

    data class InputError(val errorMessage: String) : AuthError(errorMessage)

    data class NetworkError(val errorMessage: String) : AuthError(errorMessage)
}
