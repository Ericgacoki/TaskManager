package com.dlight.eric.taskmanager.presentation.auth.event

sealed class AuthEvent {
    data class UpdateEmail(val email: String) : AuthEvent()
    data object Login : AuthEvent()
    data object Retry : AuthEvent()
    data object ClearError : AuthEvent()
    data object ResetAuthState : AuthEvent()
}
