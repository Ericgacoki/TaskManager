package com.dlight.eric.taskmanager.presentation.tasks.screen.detail

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dlight.eric.taskmanager.R
import com.dlight.eric.taskmanager.domain.model.Task
import com.dlight.eric.taskmanager.presentation.tasks.event.TaskDetailEvent
import com.dlight.eric.taskmanager.presentation.tasks.state.TaskDetailError
import com.dlight.eric.taskmanager.presentation.tasks.state.TaskDetailState
import com.dlight.eric.taskmanager.presentation.theme.TaskManagerTheme
import com.dlight.eric.taskmanager.utils.DateUtils
import com.dlight.eric.taskmanager.utils.TaskAction
import com.dlight.eric.taskmanager.utils.TaskMode
import java.util.Calendar

@Composable
fun TaskDetailScreen(
    taskId: String = "0",
    mode: TaskMode = TaskMode.VIEW,
    onNavigateBack: () -> Unit,
    viewModel: TaskDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showBackPressDialog by remember { mutableStateOf(false) }

    LaunchedEffect(taskId, mode) {
        val currentTaskId = uiState.updatedTask?.id ?: "0"
        if (currentTaskId != taskId || uiState.taskMode != mode) {
            viewModel.onEvent(TaskDetailEvent.InitializeTask(taskId, mode))
        }
    }

    LaunchedEffect(uiState.autoNavigateBack) {
        if (uiState.autoNavigateBack) {
            viewModel.onEvent(TaskDetailEvent.ClearAutoNavigationFlag)
            onNavigateBack()
        }
    }

    val handleNavigation = {
        when {
            uiState.taskMode == TaskMode.CREATE && uiState.isTaskModified -> {
                showBackPressDialog = true
            }

            uiState.taskMode == TaskMode.CREATE && !uiState.isTaskModified -> {
                viewModel.onEvent(TaskDetailEvent.PerformAction(TaskAction.CANCEL))
            }

            uiState.taskMode == TaskMode.EDIT && uiState.isTaskModified -> {
                showBackPressDialog = true
            }

            uiState.taskMode == TaskMode.EDIT && !uiState.isTaskModified -> {
                viewModel.onEvent(TaskDetailEvent.PerformAction(TaskAction.CANCEL))
            }

            else -> onNavigateBack()
        }
    }

    BackHandler(onBack = handleNavigation)

    TaskDetailScreenContent(
        uiState = uiState,
        onTitleChange = { title ->
            viewModel.onEvent(
                TaskDetailEvent.UpdateTaskInput(
                    title,
                    uiState.updatedTask?.description ?: ""
                )
            )
        },
        onDescriptionChange = { description ->
            viewModel.onEvent(
                TaskDetailEvent.UpdateTaskInput(
                    uiState.updatedTask?.title ?: "", description
                )
            )
        },
        onNavigateBack = handleNavigation,
        onEditClick = {
            viewModel.onEvent(TaskDetailEvent.PerformAction(TaskAction.EDIT))
        },
        onDeleteClick = {
            viewModel.onEvent(TaskDetailEvent.PerformAction(TaskAction.DELETE))
        },
        onSaveClick = {
            viewModel.onEvent(TaskDetailEvent.PerformAction(TaskAction.SAVE))
        },
        onCancelClick = {
            if (uiState.isTaskModified) {
                showBackPressDialog = true
            } else {
                viewModel.onEvent(TaskDetailEvent.PerformAction(TaskAction.CANCEL))
            }
        },
        onDiscardFromBackPress = {
            if (uiState.taskMode == TaskMode.CREATE) {
                onNavigateBack()
            } else {
                viewModel.onEvent(TaskDetailEvent.PerformAction(TaskAction.DISCARD))
            }
            showBackPressDialog = false
        },
        onToggleCompletion = { isChecked ->
            viewModel.onEvent(TaskDetailEvent.ToggleCompletion(isChecked))
        },
        onDueDateChange = { dateMillis ->
            viewModel.onEvent(TaskDetailEvent.UpdateDueDate(dateMillis))
        },
        showDiscardDialog = showBackPressDialog,
        onDiscardDialogDismiss = { showBackPressDialog = false })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreenContent(
    uiState: TaskDetailState,
    onTitleChange: (String) -> Unit = {},
    onDescriptionChange: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    onCancelClick: () -> Unit = {},
    onDiscardFromBackPress: () -> Unit = {},
    onToggleCompletion: (Boolean) -> Unit = {},
    onDueDateChange: (Long) -> Unit = {},
    showDeleteDialog: Boolean = false,
    showDiscardDialog: Boolean = false,
    onDeleteDialogDismiss: () -> Unit = {},
    onDiscardDialogDismiss: () -> Unit = {}
) {
    var internalShowDeleteDialog by remember { mutableStateOf(showDeleteDialog) }
    var internalShowDiscardDialog by remember { mutableStateOf(showDiscardDialog) }
    var showDatePicker by remember { mutableStateOf(false) }

    val currentDueDateMillis = uiState.updatedTask?.dueDate?.let {
        DateUtils.parseIsoString(it)
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentDueDateMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                // Only allow selecting today or future dates
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                val todayStart = calendar.timeInMillis
                return utcTimeMillis >= todayStart
            }
        })

    // Update the date picker state when the due date changes
    LaunchedEffect(currentDueDateMillis) {
        currentDueDateMillis?.let {
            datePickerState.selectedDateMillis = it
        }
    }

    LaunchedEffect(showDeleteDialog) {
        internalShowDeleteDialog = showDeleteDialog
    }

    LaunchedEffect(showDiscardDialog) {
        internalShowDiscardDialog = showDiscardDialog
    }

    val title = when (uiState.taskMode) {
        TaskMode.CREATE -> "Create Task"
        TaskMode.EDIT -> "Edit Task"
        TaskMode.VIEW -> "View Task"
    }

    Scaffold(
        topBar = {
            // Custom "AppBar" to match TaskListScreen approach
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 8.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back")
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back"
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.showEdit) {
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier.testTag("edit")
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_edit),
                                contentDescription = "Edit"
                            )
                        }
                    }

                    if (uiState.showEdit && uiState.showDelete) {
                        Spacer(Modifier.width(8.dp))
                    }

                    if (uiState.showDelete) {
                        IconButton(onClick = { internalShowDeleteDialog = true }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_delete),
                                contentDescription = "Delete"
                            )
                        }
                    }
                }
            }
        }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                if (uiState.taskMode == TaskMode.VIEW) {
                    // View mode - show as Text components
                    Text(
                        text = "Title",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = uiState.updatedTask?.title?.takeIf { it.isNotBlank() }
                        ?: "No title",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(text = uiState.updatedTask?.description?.takeIf { it.isNotBlank() }
                        ?: "No description",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                modifier = Modifier.padding(start = 4.dp),
                                text = "Due Date",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outline
                            )

                            Spacer(Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .clip(shape = RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1F))
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = uiState.updatedTask?.dueDate?.let {
                                    DateUtils.formatDueDate(DateUtils.parseIsoString(it))
                                } ?: "N/A", style = MaterialTheme.typography.bodyLarge)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .padding(top = 20.dp)
                                .width(28.dp)
                                .height(2.dp)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12F))
                        )

                        Column {
                            Text(
                                modifier = Modifier.padding(start = 4.dp),
                                text = "Updated",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outline
                            )

                            Spacer(Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .clip(shape = RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1F))
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = uiState.updatedTask?.updatedAt ?: "N/A",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                val currentlyCompleted = uiState.updatedTask?.completed ?: false
                                onToggleCompletion(!currentlyCompleted)
                            }) {
                        Checkbox(
                            checked = uiState.updatedTask?.completed ?: false,
                            onCheckedChange = null // Disable checkbox own click handling
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = if (uiState.updatedTask?.completed == true) {
                                "Task completed"
                            } else {
                                "Mark as complete"
                            }, style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    // Create/Edit mode - show input fields
                    OutlinedTextField(
                        value = uiState.updatedTask?.title ?: "",
                        onValueChange = onTitleChange,
                        maxLines = 4,
                        label = { Text("Title") },
                        isError = uiState.error is TaskDetailError.InputError,
                        supportingText = {
                            if (uiState.error is TaskDetailError.InputError) {
                                Text(
                                    text = uiState.error.message ?: "",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("title")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = uiState.updatedTask?.description ?: "",
                        onValueChange = onDescriptionChange,
                        label = { Text("Description (optional)") },
                        minLines = 4,
                        maxLines = 10,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("description")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(shape = RoundedCornerShape(4.dp))
                            .clickable {
                                showDatePicker = true
                            }, colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Calendar",
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    modifier = Modifier.padding(start = 4.dp),
                                    text = "Due Date",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )

                                Spacer(Modifier.height(4.dp))

                                Box(
                                    modifier = Modifier
                                        .clip(shape = RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1F))
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = uiState.updatedTask?.dueDate?.let {
                                            DateUtils.formatDueDate(DateUtils.parseIsoString(it))
                                        } ?: "Today",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (uiState.taskMode != TaskMode.VIEW) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (uiState.showDiscard) {
                                    internalShowDiscardDialog = true
                                } else {
                                    onCancelClick()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (uiState.showDiscard) "DISCARD" else "CANCEL")
                        }

                        Button(
                            onClick = onSaveClick,
                            enabled = uiState.canSave && !uiState.isSaving,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save")
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp), strokeWidth = 2.dp
                                )
                            } else {
                                Text("SAVE")
                            }
                        }
                    }
                }

                if (uiState.error is TaskDetailError.LoadError) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = uiState.error.message ?: "Unknown error",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }

    if (internalShowDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                internalShowDeleteDialog = false
                onDeleteDialogDismiss()
            },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete this task? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        internalShowDeleteDialog = false
                        onDeleteDialogDismiss()
                    }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        internalShowDeleteDialog = false
                        onDeleteDialogDismiss()
                    }) {
                    Text("Cancel")
                }
            })
    }

    if (internalShowDiscardDialog) {
        AlertDialog(
            onDismissRequest = {
                internalShowDiscardDialog = false
                onDiscardDialogDismiss()
            },
            title = { Text("Discard Changes") },
            text = { Text("You have unsaved changes. Are you sure you want to discard them?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDiscardFromBackPress()
                        internalShowDiscardDialog = false
                        onDiscardDialogDismiss()
                    }) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        internalShowDiscardDialog = false
                        onDiscardDialogDismiss()
                    }) {
                    Text("Keep Editing")
                }
            })
    }

    if (showDatePicker) {
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate ->
                        onDueDateChange(selectedDate)
                    }
                    showDatePicker = false
                }) {
                Text("OK")
            }
        }, dismissButton = {
            TextButton(onClick = { showDatePicker = false }) {
                Text("Cancel")
            }
        }) {
            DatePicker(
                state = datePickerState, title = {
                    Text(
                        text = "Due Date",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp)
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TaskDetailScreenPreview() {
    TaskManagerTheme(dynamicColor = false) {
        var taskMode by remember { mutableStateOf(TaskMode.CREATE) }
        var title by remember { mutableStateOf("Sample Task") }
        var description by remember { mutableStateOf("This is a sample task description") }
        var showDeleteDialog by remember { mutableStateOf(false) }
        var showDiscardDialog by remember { mutableStateOf(false) }

        val originalTask = Task(
            id = "1",
            title = "Original Task",
            description = "Original description",
            completed = false,
            dueDate = "2025-01-01T10:00:00Z",
            createdAt = "2024-01-01T10:00:00Z",
            updatedAt = "2024-01-01T10:00:00Z"
        )

        val updatedTask = Task(
            id = "1",
            title = title,
            description = description,
            completed = false,
            dueDate = "2025-01-01T10:00:00Z",
            createdAt = "2024-01-01T10:00:00Z",
            updatedAt = "2024-01-01T10:00:00Z"
        )

        TaskDetailScreenContent(
            uiState = TaskDetailState(
                originalTask = if (taskMode != TaskMode.CREATE) originalTask else null,
                updatedTask = updatedTask,
                taskMode = taskMode
            ),
            onTitleChange = { title = it },
            onDescriptionChange = { description = it },
            onEditClick = { taskMode = TaskMode.EDIT },
            onDeleteClick = { showDeleteDialog = true },
            onSaveClick = { taskMode = TaskMode.VIEW },
            onCancelClick = {
                if (taskMode == TaskMode.EDIT) taskMode = TaskMode.VIEW
            },
            showDeleteDialog = showDeleteDialog,
            showDiscardDialog = showDiscardDialog,
            onDeleteDialogDismiss = { showDeleteDialog = false },
            onDiscardDialogDismiss = { showDiscardDialog = false })
    }
}

@Preview(
    showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun TaskDetailScreenDarkPreview() {
    TaskManagerTheme(dynamicColor = false) {
        TaskDetailScreenContent(
            uiState = TaskDetailState(
                originalTask = null, updatedTask = Task(
                    id = "1",
                    title = "SHOPPING BY SECTION",
                    description = "Produce:\n" + "  • 2 lbs chicken breast, 1 lb ground\n" + "  • Bell peppers, zucchini, carrots\n" + "  • Onions, garlic, ginger\n" + "  • Lemons, limes, tomatoes, lettuce, cabbage\n" + "  • Bananas, apples\n" + "\n" + "Pantry/Dry Goods:\n" + "  • Quinoa, brown rice, basmati rice, wheat\n" + "  • Spices: curry powder\n" + "  • Flour, vanilla extract",
                    completed = false,
                    dueDate = "2025-01-01T10:00:00Z",
                    createdAt = "2024-01-01T10:00:00Z",
                    updatedAt = "45 minutes ago"
                ), taskMode = TaskMode.VIEW
            )
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TaskDetailScreenDialogsPreview() {
    TaskManagerTheme(dynamicColor = false) {
        var showDeleteDialog by remember { mutableStateOf(false) }
        var showDiscardDialog by remember { mutableStateOf(false) }

        TaskDetailScreenContent(
            uiState = TaskDetailState(
                originalTask = Task(
                    id = "1",
                    title = "Test Task",
                    description = "Test description",
                    completed = false,
                    dueDate = "2025-01-01T10:00:00Z",
                    createdAt = "2025-01-01T10:00:00Z",
                    updatedAt = "2025-01-01T10:00:00Z"
                ), updatedTask = Task(
                    id = "1",
                    title = "Modified Task",
                    description = "Click discard to preview dialog",
                    completed = false,
                    dueDate = "2025-01-01T10:00:00Z",
                    createdAt = "2025-01-01T10:00:00Z",
                    updatedAt = "2025-01-01T10:00:00Z"
                ), taskMode = TaskMode.EDIT
            ),
            showDeleteDialog = showDeleteDialog,
            showDiscardDialog = showDiscardDialog,
            onDeleteDialogDismiss = { showDeleteDialog = false },
            onDiscardDialogDismiss = { showDiscardDialog = false })
    }
}
