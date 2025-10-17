package com.dlight.eric.taskmanager.presentation.tasks.screen.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlight.eric.taskmanager.domain.model.Task
import com.dlight.eric.taskmanager.presentation.tasks.components.TaskItem

@Composable
fun TaskListScreen(
    modifier: Modifier = Modifier,
    onGotoDetail: (taskId: String, editMode: Boolean) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TaskItem(
            task = Task(
                id = "1",
                title = "Buy groceries",
                description = "Milk, eggs, bread",
                completed = false,
                createdOn = "Today",
                updatedOn = "2 hours ago"
            ),
            onEditClick = { taskId -> onGotoDetail(taskId, true) },
            onToggleComplete = { _, _ -> },
            onDeleteClick = { },
            onItemClick = { taskId -> onGotoDetail(taskId, false) }
        )
        
        TaskItem(
            task = Task(
                id = "2",
                title = "Complete project",
                description = "Finish the Android app for d.Light",
                completed = false,
                createdOn = "Yesterday",
                updatedOn = "1 hour ago"
            ),
            onEditClick = { taskId -> onGotoDetail(taskId, true) },
            onToggleComplete = { _, _ -> },
            onDeleteClick = { },
            onItemClick = { taskId -> onGotoDetail(taskId, false) }
        )
        
        TaskItem(
            task = Task(
                id = "3",
                title = "Call mom",
                description = "Weekly check-in call",
                completed = true,
                createdOn = "2 days ago",
                updatedOn = "Yesterday"
            ),
            onEditClick = { taskId -> onGotoDetail(taskId, true) },
            onToggleComplete = { _, _ -> },
            onDeleteClick = { },
            onItemClick = { taskId -> onGotoDetail(taskId, false) }
        )
    }
}
