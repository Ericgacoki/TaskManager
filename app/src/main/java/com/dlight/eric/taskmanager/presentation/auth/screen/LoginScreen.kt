package com.dlight.eric.taskmanager.presentation.auth.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dlight.eric.taskmanager.presentation.auth.event.AuthEvent
import com.dlight.eric.taskmanager.presentation.auth.state.AuthError
import com.dlight.eric.taskmanager.presentation.auth.state.AuthState
import com.dlight.eric.taskmanager.presentation.theme.TaskManagerTheme

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.authUiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) {
            onLoginSuccess()
            viewModel.onEvent(AuthEvent.ResetAuthState)
        }
    }

    LoginScreenContent(
        uiState = uiState,
        email = uiState.email,
        onEmailChange = { newEmail ->
            viewModel.onEvent(AuthEvent.UpdateEmail(newEmail))
        },
        onLoginClick = {
            when (uiState.error) {
                is AuthError.NetworkError -> viewModel.onEvent(AuthEvent.Retry)
                else -> viewModel.onEvent(AuthEvent.Login)
            }
        }
    )
}

@Composable
fun LoginScreenContent(
    uiState: AuthState,
    email: String,
    onEmailChange: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(uiState.isLoading) {
        if (uiState.isLoading) {
            focusManager.clearFocus()
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to Task Manager",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                placeholder = { Text("Enter your email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = uiState.error is AuthError.InputError,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .focusRequester(focusRequester)
                    .testTag("email"),
                enabled = !uiState.isLoading
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 40.dp)
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.error !is AuthError.None) {
                    Text(
                        text = uiState.error.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.testTag("login_error_text")
                    )
                }
            }

            Button(
                onClick = onLoginClick,
                enabled = !uiState.isLoading && uiState.error !is AuthError.InputError,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("log_in")
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = when (uiState.error) {
                            is AuthError.NetworkError -> "RETRY"
                            else -> "LOGIN"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Default State")
@Composable
fun LoginScreenPreview() {
    TaskManagerTheme(dynamicColor = false) {
        LoginScreenContent(
            uiState = AuthState(),
            email = "eric@example.com",
            onEmailChange = {},
            onLoginClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
fun LoginScreenLoadingPreview() {
    TaskManagerTheme(dynamicColor = false) {
        LoginScreenContent(
            uiState = AuthState(isLoading = true),
            email = "eric@example.com",
            onEmailChange = {},
            onLoginClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Input Error")
@Composable
fun LoginScreenInputErrorPreview() {
    TaskManagerTheme(dynamicColor = false) {
        LoginScreenContent(
            uiState = AuthState(
                error = AuthError.InputError("Please enter a valid email address")
            ),
            email = "invalid-email",
            onEmailChange = {},
            onLoginClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Network Error")
@Composable
fun LoginScreenNetworkErrorPreview() {
    TaskManagerTheme(dynamicColor = false) {
        LoginScreenContent(
            uiState = AuthState(error = AuthError.NetworkError("Unable to connect. Please try again.")),
            email = "user@example.com",
            onEmailChange = {},
            onLoginClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Interactive Email State")
@Composable
fun LoginScreenInteractivePreview() {
    var email by remember { mutableStateOf("") }
    
    TaskManagerTheme(dynamicColor = false) {
        LoginScreenContent(
            uiState = AuthState(),
            email = email,
            onEmailChange = { email = it },
            onLoginClick = {}
        )
    }
}