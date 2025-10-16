package com.dlight.eric.taskmanager.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object DateUtils {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    
    private val dayMonthFormat = SimpleDateFormat("EEE MMM d", Locale.US)
    
    /**
     * Formats a timestamp to a human-readable string
     * 
     * Examples:
     * - today: "Today"
     * - yesterday: "Yesterday"  
     * - 3 days ago: "Sat Dec 14"
     * - 8 days ago: "Last week"
     * - 15 days ago: "2 weeks ago"
     * - 35 days ago: "Last month"
     * - 65 days ago: "2 months ago"
     * - 400 days ago: "Last year"
     * - 800 days ago: "2 years ago"
     */
    fun formatToString(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        
        val calendar = Calendar.getInstance()
        val todayDay = calendar.get(Calendar.DAY_OF_YEAR)
        val todayYear = calendar.get(Calendar.YEAR)
        
        calendar.timeInMillis = timestamp
        val dateDay = calendar.get(Calendar.DAY_OF_YEAR)
        val dateYear = calendar.get(Calendar.YEAR)
        
        return when {
            dateYear == todayYear && dateDay == todayDay -> "Today"
            dateYear == todayYear && dateDay == todayDay - 1 -> "Yesterday"
            days in 2..6 -> dayMonthFormat.format(Date(timestamp))
            days in 7..13 -> "Last week"
            days in 14..20 -> "2 weeks ago"
            days in 21..27 -> "3 weeks ago"
            days in 28..59 -> "Last month"
            days in 60..89 -> "2 months ago"
            days >= 90 -> {
                val months = days / 30
                if (months >= 12) {
                    val years = months / 12
                    if (years == 1L) "Last year" else "$years years ago"
                } else {
                    "$months months ago"
                }
            }
            else -> dayMonthFormat.format(Date(timestamp))
        }
    }
    
    fun parseIsoString(dateString: String): Long {
        return try {
            isoFormat.parse(dateString)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
    
    fun formatToIsoString(timestamp: Long): String {
        return isoFormat.format(Date(timestamp))
    }
}
