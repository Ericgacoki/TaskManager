package com.dlight.eric.taskmanager.presentation.tasks.event

sealed class TaskEvent {

    data object LoadTasks : TaskEvent()

    data class ToggleTaskCompletion(val taskId: String, val isCompleted: Boolean) : TaskEvent()

    data object PullToRefresh : TaskEvent()

    data object Retry : TaskEvent()

    data class DeleteTask(val taskId: String) : TaskEvent()

    data object DeleteAll : TaskEvent()

    data object LogOut : TaskEvent()
}
