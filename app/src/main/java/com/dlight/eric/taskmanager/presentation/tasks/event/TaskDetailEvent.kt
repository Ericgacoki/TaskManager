package com.dlight.eric.taskmanager.presentation.tasks.event

import com.dlight.eric.taskmanager.utils.TaskAction
import com.dlight.eric.taskmanager.utils.TaskMode

sealed class TaskDetailEvent {

    data class InitializeTask(val taskId: String, val mode: TaskMode) : TaskDetailEvent()

    data class UpdateTaskInput(val title: String, val description: String) : TaskDetailEvent()

    data class PerformAction(val action: TaskAction) : TaskDetailEvent()

    data class ToggleCompletion(val isCompleted: Boolean) : TaskDetailEvent()

    data object ClearAutoNavigationFlag : TaskDetailEvent()
}
