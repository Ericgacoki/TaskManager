package com.dlight.eric.taskmanager.presentation.tasks.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dlight.eric.taskmanager.R
import com.dlight.eric.taskmanager.presentation.tasks.state.SyncState
import com.dlight.eric.taskmanager.presentation.theme.TaskManagerTheme

@Composable
fun SyncStateHeader(
    unSyncedCount: Int,
    syncState: SyncState,
    syncError: String? = null,
    onSyncClick: () -> Unit = {}
) {
    val containerColor = when (syncState) {
        SyncState.FAILED -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                when (syncState) {
                    SyncState.SYNCING -> {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Syncing...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    SyncState.FAILED -> {
                        Icon(
                            painter = painterResource(R.drawable.ic_cloud_off),
                            contentDescription = "Sync Failed",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = syncError ?: "Sync failed",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    SyncState.PENDING -> {
                        Icon(
                            painter = painterResource(R.drawable.ic_cloud_off),
                            contentDescription = "UnSynced",
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "$unSyncedCount ${if (unSyncedCount == 1) "task" else "tasks"} to sync",
                            style = MaterialTheme.typography.bodyMedium,
                            color= MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    SyncState.SYNCED -> {
                        // This state shouldn't show the header, but handle it just in case
                        Icon(
                            painter = painterResource(R.drawable.ic_cloud_off),
                            contentDescription = "UnSynced",
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .size(20.dp)
                        )
                        Text(
                            text = "All tasks synced",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            when (syncState) {
                SyncState.SYNCING -> {
                    // No button during sync
                }

                SyncState.FAILED -> {
                    TextButton(
                        onClick = onSyncClick
                    ) {
                        Text(
                            text = "Retry",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                SyncState.PENDING -> {
                    TextButton(
                        onClick = onSyncClick
                    ) {
                        Text(
                            text = "Sync",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                SyncState.SYNCED -> {
                    // No button when synced
                }
            }
        }
    }
}

@Preview
@Composable
private fun PendingPreview() {
    TaskManagerTheme(dynamicColor = false) {
        SyncStateHeader(
            unSyncedCount = 3,
            syncState = SyncState.PENDING,
            onSyncClick = {}
        )
    }
}

@Preview
@Composable
private fun SyncingPreview() {
    TaskManagerTheme(dynamicColor = false) {
        SyncStateHeader(
            unSyncedCount = 3,
            syncState = SyncState.SYNCING,
            onSyncClick = {}
        )
    }
}

@Preview
@Composable
private fun FailedPreview() {
    TaskManagerTheme(dynamicColor = false) {
        SyncStateHeader(
            unSyncedCount = 0,
            syncState = SyncState.FAILED,
            syncError = "Network connection failed",
            onSyncClick = {}
        )
    }
}
