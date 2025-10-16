package com.dlight.eric.taskmanager.presentation.tasks.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.dlight.eric.taskmanager.R
import com.dlight.eric.taskmanager.presentation.theme.TaskManagerTheme

@Composable
fun StatusBadge(
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = MaterialTheme.colorScheme.secondaryContainer
    val textColor = MaterialTheme.colorScheme.onSecondaryContainer
    
    val statusText = if (isCompleted) "COMPLETE" else "PENDING"
    val statusIconRes = if (isCompleted) R.drawable.ic_done else R.drawable.ic_pending

    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = statusIconRes),
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp
            )
        }
    }
}

@PreviewLightDark
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StatusBadgePreview() {
    TaskManagerTheme(dynamicColor = false) {
        Surface {
            Column( modifier = Modifier.padding(16.dp)) {
                StatusBadge(isCompleted = false)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                StatusBadge(isCompleted = true)
            }
        }
    }
}