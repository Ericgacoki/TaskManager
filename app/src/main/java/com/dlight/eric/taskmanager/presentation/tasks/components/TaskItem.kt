package com.dlight.eric.taskmanager.presentation.tasks.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dlight.eric.taskmanager.R
import com.dlight.eric.taskmanager.domain.model.Task
import com.dlight.eric.taskmanager.presentation.theme.TaskManagerTheme
import com.dlight.eric.taskmanager.utils.DateUtils

@Composable
fun TaskItem(
    task: Task,
    onEditClick: (taskId: String) -> Unit,
    onToggleComplete: (taskId: String, isCompleted: Boolean) -> Unit,
    onDeleteClick: (taskId: String) -> Unit,
    onItemClick: (taskId: String) -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    val readableDate = remember(task.updatedAt) {
        DateUtils.formatToReadableDate(task.updatedAt)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = CardDefaults.shape)
            .clickable { onItemClick(task.id) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    modifier = Modifier.weight(1f, fill = false),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Menu"
                    )

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_edit),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            onClick = {
                                onEditClick(task.id)
                                showMenu = false
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(if (task.completed) "Mark as Pending" else "Mark as Complete")
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(
                                        id = if (task.completed) R.drawable.ic_pending else R.drawable.ic_done
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            onClick = {
                                onToggleComplete(task.id, !task.completed)
                                showMenu = false
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Delete") },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_delete),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            onClick = {
                                onDeleteClick(task.id)
                                showMenu = false
                            }
                        )
                    }
                }
            }

            if (task.description.isNotEmpty()) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 10,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(
                    isCompleted = task.completed,
                    modifier = Modifier
                )

                Text(
                    text = readableDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun TaskItemPreview() {
    TaskManagerTheme(dynamicColor = false) {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Pending task with description
                TaskItem(
                    task = Task(
                        id = "1",
                        title = "Buy groceries for the week including fruits and vegetables",
                        description = "Need to get milk, eggs, bread, apples, spinach, and other essentials for the upcoming week",
                        completed = false,
                        createdAt = "2025-01-19T08:00:00Z",
                        updatedAt = "2025-01-19T14:00:00Z",
                        dueDate = "Today"
                    ),
                    onEditClick = { },
                    onToggleComplete = { _, _ -> },
                    onDeleteClick = { },
                    onItemClick = { }
                )

                // Completed task without description
                TaskItem(
                    task = Task(
                        id = "2",
                        title = "Complete project documentation",
                        description = "",
                        completed = true,
                        createdAt = "2025-01-18T10:00:00Z",
                        updatedAt = "2025-01-18T16:00:00Z",
                        dueDate = "Today"
                    ),
                    onEditClick = {},
                    onToggleComplete = { _, _ -> },
                    onDeleteClick = {},
                    onItemClick = {}
                )

                // Pending task with very long title
                TaskItem(
                    task = Task(
                        id = "3",
                        title = "This is a very long task title that should be truncated with ellipsis when it exceeds the available space",
                        description = "Short description here",
                        completed = false,
                        createdAt = "2025-01-12T09:00:00Z",
                        updatedAt = "2025-01-17T11:30:00Z",
                        dueDate = "Today"
                    ),
                    onEditClick = {},
                    onToggleComplete = { _, _ -> },
                    onDeleteClick = {},
                    onItemClick = {}
                )
            }
        }
    }
}
