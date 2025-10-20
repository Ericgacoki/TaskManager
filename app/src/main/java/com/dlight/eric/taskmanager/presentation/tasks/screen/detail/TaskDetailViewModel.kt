package com.dlight.eric.taskmanager.presentation.tasks.screen.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dlight.eric.taskmanager.domain.model.Task
import com.dlight.eric.taskmanager.domain.repository.TaskRepository
import com.dlight.eric.taskmanager.presentation.tasks.event.TaskDetailEvent
import com.dlight.eric.taskmanager.presentation.tasks.state.TaskDetailError
import com.dlight.eric.taskmanager.presentation.tasks.state.TaskDetailState
import com.dlight.eric.taskmanager.utils.DateUtils
import com.dlight.eric.taskmanager.utils.Resource
import com.dlight.eric.taskmanager.utils.TaskAction
import com.dlight.eric.taskmanager.utils.TaskMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskDetailState())
    val uiState: StateFlow<TaskDetailState> = _uiState.asStateFlow()

    fun onEvent(event: TaskDetailEvent) {
        when (event) {
            is TaskDetailEvent.InitializeTask -> initializeTask(event.taskId, event.mode)
            is TaskDetailEvent.UpdateTaskInput -> updateTask(event.title, event.description)
            is TaskDetailEvent.UpdateDueDate -> updateDueDate(event.dueDateMillis)
            is TaskDetailEvent.PerformAction -> handleTaskAction(event.action)
            is TaskDetailEvent.ToggleCompletion -> toggleTaskCompletion(event.isCompleted)
            is TaskDetailEvent.ClearAutoNavigationFlag -> clearAutoNavigationFlag()
        }
    }

    private fun handleTaskAction(action: TaskAction) {
        when (action) {
            TaskAction.SAVE -> if (_uiState.value.canSave) saveTask()
            TaskAction.EDIT -> if (_uiState.value.showEdit) startEditMode()
            TaskAction.DELETE -> if (_uiState.value.showDelete) deleteTask()
            TaskAction.DISCARD -> if (_uiState.value.showDiscard) discardChanges()
            TaskAction.CANCEL -> cancelEdit()
        }
    }

    private fun createEmptyTask(): Task {
        val currentTime = System.currentTimeMillis()
        
        // Get today's date at 12:00 PM for due date only (to avoid timezone edge cases)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayNoon = calendar.timeInMillis

        return Task(
            id = UUID.randomUUID().toString(),
            title = "",
            description = "",
            completed = false,
            dueDate = DateUtils.formatToIsoString(todayNoon),
            createdAt = DateUtils.formatToIsoString(currentTime),
            updatedAt = DateUtils.formatToIsoString(currentTime)
        )
    }

    private fun initializeTask(taskId: String, mode: TaskMode) {
        when (mode) {
            TaskMode.CREATE -> {
                val newTask = createEmptyTask()
                _uiState.update {
                    it.copy(
                        updatedTask = newTask,
                        originalTask = newTask,
                        taskMode = TaskMode.CREATE
                    )
                }
            }

            TaskMode.EDIT, TaskMode.VIEW -> {
                // EDIT and VIEW modes always have a valid taskId
                _uiState.update { it.copy(taskMode = mode) }
                loadTask(taskId)
            }
        }
    }

    private fun loadTask(taskId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = TaskDetailError.None) }

            taskRepository.getTaskById(taskId)
                .collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }

                        is Resource.Success -> {
                            val task = resource.data
                            if (task != null) {
                                _uiState.update {
                                    it.copy(
                                        updatedTask = task,
                                        originalTask = task,
                                        isLoading = false,
                                        error = TaskDetailError.None
                                    )
                                }
                            } else {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        error = TaskDetailError.LoadError("Task not found")
                                    )
                                }
                            }
                        }

                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = TaskDetailError.LoadError(resource.message ?: "Error")
                                )
                            }
                        }
                    }
                }
        }
    }

    private fun updateTask(title: String, description: String) {
        _uiState.update { state ->
            val currentTask = state.updatedTask ?: return@update state
            val updatedTask = currentTask.copy(
                title = title,
                description = description
            )
            state.copy(
                updatedTask = updatedTask,
                error = TaskDetailError.None
            )
        }
    }

    private fun updateDueDate(dueDateMillis: Long) {
        _uiState.update { state ->
            val currentTask = state.updatedTask ?: return@update state
            val updatedTask = currentTask.copy(
                dueDate = DateUtils.formatToIsoString(dueDateMillis)
            )
            state.copy(
                updatedTask = updatedTask,
                error = TaskDetailError.None
            )
        }
    }

    private fun saveTask() {
        val currentTask = _uiState.value.updatedTask ?: return

        if (currentTask.title.isBlank()) {
            _uiState.update {
                it.copy(
                    error = TaskDetailError.InputError("Task title cannot be empty")
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = TaskDetailError.None) }

            // Update timestamp for all save operations (CREATE and EDIT)
            val taskToSave = when (_uiState.value.taskMode) {
                TaskMode.CREATE, TaskMode.EDIT -> {
                    currentTask.copy(
                        title = currentTask.title.trim(),
                        description = currentTask.description.trim(),
                        updatedAt = DateUtils.formatToIsoString(System.currentTimeMillis())
                    )
                }

                TaskMode.VIEW -> currentTask
            }

            // Update UI state with the timestamped task before saving
            if (_uiState.value.taskMode != TaskMode.VIEW) {
                _uiState.update { it.copy(updatedTask = taskToSave) }
            }

            val result = when (_uiState.value.taskMode) {
                TaskMode.CREATE -> {
                    taskRepository.insertTask(taskToSave)
                }

                TaskMode.EDIT -> {
                    taskRepository.updateTask(taskToSave)
                }

                TaskMode.VIEW -> {
                    Resource.Success(Unit)
                }
            }

            when (result) {
                is Resource.Success -> {
                    // After successful save, update the original task reference
                    val savedTask = _uiState.value.updatedTask
                    _uiState.update {
                        it.copy(
                            originalTask = savedTask,
                            isSaving = false,
                            taskMode = TaskMode.VIEW
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isLoading = false,
                            error = TaskDetailError.LoadError(result.message ?: "Unknown error")
                        )
                    }
                }

                is Resource.Loading -> {}
            }
        }
    }

    private fun deleteTask() {
        if (_uiState.value.taskMode == TaskMode.CREATE) {
            return
        }

        val currentTask = _uiState.value.updatedTask ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = TaskDetailError.None) }

            when (val result = taskRepository.deleteTask(currentTask)) {
                is Resource.Success -> {
                    // After successful delete, signal navigation back
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isLoading = false,
                            error = TaskDetailError.None,
                            autoNavigateBack = true
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = TaskDetailError.LoadError(result.message ?: "Unknown error")
                        )
                    }
                }

                is Resource.Loading -> {}
            }
        }
    }

    private fun startEditMode() {
        _uiState.update { it.copy(taskMode = TaskMode.EDIT) }
    }

    private fun discardChanges() {
        _uiState.update { state ->
            state.copy(
                updatedTask = state.originalTask,
                taskMode = TaskMode.VIEW,
                error = TaskDetailError.None
            )
        }
    }

    private fun cancelEdit() {
        when (_uiState.value.taskMode) {
            TaskMode.CREATE -> {
                _uiState.update { it.copy(autoNavigateBack = true) }
            }

            TaskMode.EDIT -> {
                _uiState.update { it.copy(taskMode = TaskMode.VIEW) }
            }

            TaskMode.VIEW -> {
                // Never gonna happen...
            }
        }
    }

    private fun toggleTaskCompletion(isCompleted: Boolean) {
        val currentTask = _uiState.value.updatedTask ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = TaskDetailError.None) }

            when (val result = taskRepository.updateTaskCompletion(currentTask.id, isCompleted)) {
                is Resource.Success -> {
                    val updatedTask = currentTask.copy(completed = isCompleted)
                    _uiState.update {
                        it.copy(
                            updatedTask = updatedTask,
                            originalTask = updatedTask,
                            isSaving = false,
                            error = TaskDetailError.None
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = TaskDetailError.LoadError(
                                result.message ?: "Failed to update task"
                            )
                        )
                    }
                }

                is Resource.Loading -> {}
            }
        }
    }

    private fun clearAutoNavigationFlag() {
        _uiState.update { it.copy(autoNavigateBack = false) }
    }
}
