package com.dlight.eric.taskmanager.presentation.tasks.screen.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.dlight.eric.taskmanager.utils.TaskMode

@Composable
fun TaskDetailScreen(
    taskId: String = "0",
    mode: TaskMode = TaskMode.VIEW
) {
    LaunchedEffect(taskId) {
        // Fetch this Task from database based on TaskMode
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val modeText = when (mode) {
            TaskMode.CREATE -> "Create Mode"
            TaskMode.EDIT -> "Edit Mode"
            TaskMode.VIEW -> "View Mode"
        }
        Text(text = "Details for: $taskId ($modeText)")
    }
}
