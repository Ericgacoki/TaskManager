package com.dlight.eric.taskmanager.presentation.tasks.state

import com.dlight.eric.taskmanager.domain.model.Task
import com.dlight.eric.taskmanager.utils.TaskMode

data class TaskDetailState(
    val originalTask: Task? = null,
    val updatedTask: Task? = null,
    val taskMode: TaskMode = TaskMode.VIEW,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: TaskDetailError = TaskDetailError.None,
    val autoNavigateBack: Boolean = false
) {
    val isTaskModified: Boolean = originalTask != updatedTask
    val canSave: Boolean = taskMode in listOf(TaskMode.CREATE, TaskMode.EDIT) && isTaskModified
    val showDiscard: Boolean = taskMode == TaskMode.EDIT && isTaskModified
    val showEdit: Boolean = taskMode == TaskMode.VIEW && updatedTask != null
    val showDelete: Boolean = taskMode == TaskMode.VIEW && updatedTask != null
}
