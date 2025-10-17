package com.dlight.eric.taskmanager.presentation.tasks.state

sealed class TaskDetailError(val message: String? = null) {

    data object None : TaskDetailError()

    data class InputError(val errorMessage: String) : TaskDetailError(errorMessage)

    data class LoadError(val errorMessage: String) : TaskDetailError(errorMessage)
}
