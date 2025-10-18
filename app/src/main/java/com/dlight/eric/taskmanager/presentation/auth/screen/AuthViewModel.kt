package com.dlight.eric.taskmanager.presentation.auth.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dlight.eric.taskmanager.domain.repository.AuthRepository
import com.dlight.eric.taskmanager.presentation.auth.event.AuthEvent
import com.dlight.eric.taskmanager.presentation.auth.state.AuthState
import com.dlight.eric.taskmanager.presentation.auth.state.AuthError
import com.dlight.eric.taskmanager.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authUiState = MutableStateFlow(AuthState())
    val authUiState: StateFlow<AuthState> = _authUiState.asStateFlow()

    fun isLoggedIn(): Flow<Resource<Boolean>> {
        return authRepository.getLoggedInUserToken().map { result ->
            when (result) {
                is Resource.Loading -> Resource.Loading()
                is Resource.Success -> Resource.Success(!result.data.isNullOrEmpty())
                is Resource.Error -> Resource.Success(false) // On error, treat as not logged in
            }
        }
    }

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.UpdateEmail -> updateEmail(event.email)
            is AuthEvent.Login -> login()
            is AuthEvent.Retry -> retry()
            is AuthEvent.ClearError -> clearError()
            is AuthEvent.ResetAuthState -> resetAuthState()
        }
    }

    private fun updateEmail(email: String) {
        _authUiState.value = _authUiState.value.copy(
            email = email,
            error = AuthError.None
        )
    }

    private fun login() {
        val email = _authUiState.value.email
        
        if (email.isBlank()) {
            _authUiState.value = _authUiState.value.copy(
                error = AuthError.InputError("Email cannot be empty")
            )
            return
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authUiState.value = _authUiState.value.copy(
                error = AuthError.InputError("Please enter a valid email address")
            )
            return
        }

        viewModelScope.launch {
            authRepository.login(email).collectLatest { result ->
                when (result) {
                    is Resource.Loading -> {
                        _authUiState.value = _authUiState.value.copy(
                            isLoading = true,
                            error = AuthError.None
                        )
                    }

                    is Resource.Success -> {
                        _authUiState.value = _authUiState.value.copy(
                            isLoading = false,
                            loginSuccess = true,
                            error = AuthError.None
                        )
                    }

                    is Resource.Error -> {
                        _authUiState.value = _authUiState.value.copy(
                            isLoading = false,
                            error = AuthError.NetworkError(result.message ?: "Unable to connect. Please try again.")
                        )
                    }
                }
            }
        }
    }

    private fun retry() {
        _authUiState.value = _authUiState.value.copy(error = AuthError.None)
        login() // again haha 😸
    }

    private fun clearError() {
        _authUiState.value = _authUiState.value.copy(error = AuthError.None)
    }

    private fun resetAuthState() {
        _authUiState.value = AuthState()
    }
}