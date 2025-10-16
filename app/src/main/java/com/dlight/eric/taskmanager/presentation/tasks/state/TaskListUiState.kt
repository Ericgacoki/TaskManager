package com.dlight.eric.taskmanager.presentation.tasks.state

import com.dlight.eric.taskmanager.domain.model.Task

data class TaskListUiState(
    val isLoading: Boolean = true,
    val tasks: List<Task> = emptyList(),
    val error: String? = null,
    val lastSyncTime: String? = null,
    val isPullingToRefresh: Boolean = false
)
