package com.dlight.eric.taskmanager.presentation.auth.state

data class AuthState(
    val email: String = "",
    val isLoading: Boolean = false,
    val error: AuthError = AuthError.None,
    val loginSuccess: Boolean = false
)
