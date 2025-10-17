package com.dlight.eric.taskmanager.presentation.tasks.screen.list

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dlight.eric.taskmanager.R
import com.dlight.eric.taskmanager.domain.model.Task
import com.dlight.eric.taskmanager.presentation.tasks.components.TaskItem
import com.dlight.eric.taskmanager.presentation.tasks.event.TaskEvent
import com.dlight.eric.taskmanager.presentation.tasks.state.TaskListUiState
import com.dlight.eric.taskmanager.presentation.theme.TaskManagerTheme
import com.dlight.eric.taskmanager.utils.LottieEmptyState
import com.dlight.eric.taskmanager.utils.ShowSnackbar
import com.dlight.eric.taskmanager.utils.TaskMode

@Composable
fun TaskListScreen(
    onNavigateToTask: (taskId: String, mode: TaskMode) -> Unit,
    onLogOut: () -> Unit,
    viewModel: TaskListViewModel = hiltViewModel()
) {
    val uiState by viewModel.tasksUiState.collectAsStateWithLifecycle()

    TaskListContent(
        uiState = uiState,
        onNavigateToTask = onNavigateToTask,
        onToggleComplete = { taskId, isCompleted ->
            viewModel.onEvent(TaskEvent.ToggleTaskCompletion(taskId, isCompleted))
        },
        onDeleteTask = { taskId ->
            viewModel.onEvent(TaskEvent.DeleteTask(taskId))
        },
        onClickAddTask = { onNavigateToTask("0", TaskMode.CREATE) },
        onRetry = { viewModel.onEvent(TaskEvent.Retry) },
        onDeleteAll = { viewModel.onEvent(TaskEvent.DeleteAll) },
        onLogOut = {
            viewModel.onEvent(TaskEvent.LogOut)
            onLogOut()
        },
        onPullToRefresh = { viewModel.onEvent(TaskEvent.PullToRefresh) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListContent(
    uiState: TaskListUiState,
    onNavigateToTask: (taskId: String, mode: TaskMode) -> Unit,
    onToggleComplete: (taskId: String, isCompleted: Boolean) -> Unit,
    onDeleteTask: (taskId: String) -> Unit,
    onClickAddTask: () -> Unit,
    onRetry: () -> Unit,
    onDeleteAll: () -> Unit,
    onLogOut: () -> Unit,
    onPullToRefresh: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var showLogOutDialog by remember { mutableStateOf(false) }

    ShowSnackbar(
        snackbarHostState = snackbarHostState,
        message = uiState.error,
        actionLabel = "Retry",
        onAction = onRetry
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // A custom "AppBar" since AppTopBar introduces weirds upper padding
            Row(
                modifier = Modifier
                    // .background(Color.Red)
                    .fillMaxWidth()
                    // .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(
                        top = 16.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Tasks",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    if (uiState.tasks.isNotEmpty()) {
                        val text = if (uiState.lastSyncTime == null) "Not Synced" else
                            "Last Synced ${uiState.lastSyncTime}"
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.tasks.isNotEmpty()) {
                        IconButton(onClick = { showDeleteAllDialog = true }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_delete),
                                contentDescription = "Delete All"
                            )
                        }

                        Spacer(Modifier.width(12.dp))
                    }

                    IconButton(onClick = { showLogOutDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_log_out),
                            contentDescription = "Log Out"
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onClickAddTask
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task"
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isPullingToRefresh,
            onRefresh = onPullToRefresh,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.tasks.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 16.dp, end = 16.dp, bottom = 100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.error != null) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = uiState.error,
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )

                                Button(
                                    onClick = onRetry
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )

                                    Text("RETRY")
                                }
                            }
                        } else {
                            LottieEmptyState(
                                animationRes = R.raw.empty_state,
                                title = "No tasks yet",
                                subtitle = "Tap the + button to create your first task"
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.tasks, key = { task -> task.id }) { task ->
                            TaskItem(
                                task = task,
                                onEditClick = { taskId -> onNavigateToTask(taskId, TaskMode.EDIT) },
                                onToggleComplete = onToggleComplete,
                                onDeleteClick = onDeleteTask,
                                onItemClick = { taskId -> onNavigateToTask(taskId, TaskMode.VIEW) }
                            )
                        }

                        item {
                            Spacer(Modifier.height(100.dp))
                        }
                    }
                }
            }
        }
    }

    // Delete All Confirmation Dialog
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Delete All Tasks") },
            text = { Text("Are you sure you want to delete all tasks? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAllDialog = false
                        onDeleteAll()
                    }
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Log Out Confirmation Dialog
    if (showLogOutDialog) {
        AlertDialog(
            onDismissRequest = { showLogOutDialog = false },
            title = { Text("Log Out") },
            text = { Text("Are you sure you want to log out? You'll need to log in again to access your tasks.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogOutDialog = false
                        onLogOut()
                    }
                ) {
                    Text("Log Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Empty State"
)
@Composable
fun TaskListEmptyPreview() {
    TaskManagerTheme(dynamicColor = false) {
        Surface {
            TaskListContent(
                uiState = TaskListUiState(
                    isLoading = false,
                    tasks = emptyList()
                ),
                onNavigateToTask = { _, _ -> },
                onToggleComplete = { _, _ -> },
                onDeleteTask = {},
                onClickAddTask = {},
                onRetry = {},
                onDeleteAll = {},
                onLogOut = {},
                onPullToRefresh = {}
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Content State")
@Composable
fun TaskListContentPreview() {
    TaskManagerTheme(dynamicColor = false) {
        TaskListContent(
            uiState = TaskListUiState(
                isLoading = false,
                lastSyncTime = "5 min ago",
                isPullingToRefresh = true,
                tasks = listOf(
                    Task(
                        id = "1",
                        title = "Buy groceries",
                        description = "Milk, eggs, bread",
                        completed = false,
                        createdAt = "Today",
                        updatedAt = "2 hours ago",
                        dueDate = "Today"
                    ),
                    Task(
                        id = "2",
                        title = "Complete project",
                        description = "Finish the Android app for d.Light",
                        completed = false,
                        createdAt = "Yesterday",
                        updatedAt = "1 hour ago",
                        dueDate = "Today"
                    ),
                    Task(
                        id = "3",
                        title = "Call mom",
                        description = "Weekly check-in call",
                        completed = true,
                        createdAt = "2 days ago",
                        updatedAt = "Yesterday",
                        dueDate = "Today"
                    )
                )
            ),
            onNavigateToTask = { _, _ -> },
            onToggleComplete = { _, _ -> },
            onDeleteTask = {},
            onClickAddTask = {},
            onRetry = {},
            onDeleteAll = {},
            onLogOut = {},
            onPullToRefresh = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Error State")
@Composable
fun TaskListErrorPreview() {
    TaskManagerTheme(dynamicColor = false) {
        TaskListContent(
            uiState = TaskListUiState(
                isLoading = false,
                error = "Failed to load tasks. Database might be corrupted...lol",
                tasks = emptyList()
            ),
            onNavigateToTask = { _, _ -> },
            onToggleComplete = { _, _ -> },
            onDeleteTask = {},
            onClickAddTask = {},
            onRetry = {},
            onDeleteAll = {},
            onLogOut = {},
            onPullToRefresh = {}
        )
    }
}
