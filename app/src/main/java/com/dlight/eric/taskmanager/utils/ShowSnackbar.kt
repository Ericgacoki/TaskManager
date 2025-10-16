package com.dlight.eric.taskmanager.utils

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun ShowSnackbar(
    snackbarHostState: SnackbarHostState,
    message: String?,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Short,
    onAction: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null
) {
    LaunchedEffect(message) {
        message?.let {
            val result = snackbarHostState.showSnackbar(
                message = it,
                actionLabel = actionLabel,
                duration = duration
            )
            when (result) {
                SnackbarResult.ActionPerformed -> onAction?.invoke()
                SnackbarResult.Dismissed -> onDismiss?.invoke()
            }
        }
    }
}
