package com.dlight.eric.taskmanager.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun parseIsoString(dateString: String): Long {
        return try {
            isoFormat.parse(dateString)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    /**
     * This returns a date in the format "2025-10-06T09:30:00Z"
     * */
    fun formatToIsoString(timestamp: Long): String {
        return isoFormat.format(Date(timestamp))
    }
    
    /**
     * Formats a timestamp to a string
     * 
     * Examples:
     * - "Just now" (< 1 minute)
     * - "5 minutes ago"
     * - "2 hours ago" 
     * - "3 days ago"
     * - "Jul 10, 2025" (> 1 week)
     */
    fun formatTimestamp(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60_000 -> "Just now"
            diff < 3600_000 -> {
                val minutes = diff / 60_000
                if (minutes == 1L) "1 minute ago" else "$minutes minutes ago"
            }
            diff < 86400_000 -> {
                val hours = diff / 3600_000
                if (hours == 1L) "1 hour ago" else "$hours hours ago"
            }
            diff < 604800_000 -> {
                val days = diff / 86400_000
                if (days == 1L) "1 day ago" else "$days days ago"
            }
            else -> {
                val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                formatter.format(Date(timestamp))
            }
        }
    }

    /**
     * Formats a timestamp to a date string for due dates
     * 
     * Examples:
     * - "Today" (same day)
     * - "Tomorrow" (next day)
     * - "Yesterday" (previous day)  
     * - "Jan 15, 2025" (other dates)
     */
    fun formatDueDate(timestamp: Long): String {
        // Compare calendar dates, not timestamps for accuracy.
        val todayCalendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        
        val dueCalendar = java.util.Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        
        // Calculate difference in days
        val daysDiff = ((dueCalendar.timeInMillis - todayCalendar.timeInMillis) / 86400_000L).toInt()
        
        return when (daysDiff) {
            -1 -> "Yesterday"
            0 -> "Today"
            1 -> "Tomorrow"
            else -> {
                val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                formatter.format(Date(timestamp))
            }
        }
    }
}
