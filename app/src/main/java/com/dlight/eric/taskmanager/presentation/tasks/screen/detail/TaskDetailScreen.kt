package com.dlight.eric.taskmanager.presentation.tasks.screen.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun TaskDetailScreen(
    taskId: String = "",
    editMode: Boolean = false
) {
    LaunchedEffect(taskId) {
        // Fetch this Task from database
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Details for: $taskId ${if (editMode) "(Edit Mode)" else "(View Mode)"}")
    }
}
