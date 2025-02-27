package wesseling.io.fasttime.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import wesseling.io.fasttime.model.DateTimePreferences

/**
 * Utility class for formatting dates and times
 */
object DateTimeFormatter {
    
    /**
     * Format a timestamp as a date string
     */
    fun formatDate(timestamp: Long, preferences: DateTimePreferences): String {
        val date = Date(timestamp)
        val pattern = preferences.getDatePattern()
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        return formatter.format(date)
    }
    
    /**
     * Format a timestamp as a time string
     */
    fun formatTime(timestamp: Long, preferences: DateTimePreferences): String {
        val date = Date(timestamp)
        val pattern = preferences.getTimePattern()
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        return formatter.format(date)
    }
    
    /**
     * Format a timestamp as a date and time string
     */
    fun formatDateTime(timestamp: Long, preferences: DateTimePreferences): String {
        val date = Date(timestamp)
        val pattern = preferences.getDateTimePattern()
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        return formatter.format(date)
    }
    
    /**
     * Format a timestamp as a date and time string (legacy method)
     */
    fun formatDateTime(timestamp: Long): String {
        val date = Date(timestamp)
        val pattern = "MMM d, yyyy HH:mm"
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        return formatter.format(date)
    }
    
    /**
     * Format elapsed time in hours and minutes
     */
    fun formatElapsedTime(elapsedTimeMillis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(elapsedTimeMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTimeMillis) % 60
        
        return if (hours > 0) {
            String.format("%d h %02d min", hours, minutes)
        } else {
            String.format("%d min", minutes)
        }
    }
    
    /**
     * Format a time duration in a human-readable format
     */
    fun formatDuration(durationMillis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(durationMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60
        
        return when {
            hours > 0 -> "$hours h $minutes min"
            else -> "$minutes min"
        }
    }
} 