package wesseling.io.fasttime.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import wesseling.io.fasttime.model.DateTimePreferences
import android.content.Context
import wesseling.io.fasttime.R

/**
 * Utility class for formatting dates and times
 */
object DateTimeFormatter {
    
    /**
     * Format a timestamp as a date string with specified locale
     */
    fun formatDate(timestamp: Long, preferences: DateTimePreferences, locale: Locale = Locale.getDefault()): String {
        val date = Date(timestamp)
        val pattern = preferences.getDatePattern()
        val formatter = SimpleDateFormat(pattern, locale)
        return formatter.format(date)
    }
    
    /**
     * Format a timestamp as a time string with specified locale
     */
    fun formatTime(timestamp: Long, preferences: DateTimePreferences, locale: Locale = Locale.getDefault()): String {
        val date = Date(timestamp)
        val pattern = preferences.getTimePattern()
        val formatter = SimpleDateFormat(pattern, locale)
        return formatter.format(date)
    }
    
    /**
     * Format a timestamp as a date and time string with specified locale
     */
    fun formatDateTime(timestamp: Long, preferences: DateTimePreferences, locale: Locale = Locale.getDefault()): String {
        val date = Date(timestamp)
        val pattern = preferences.getDateTimePattern()
        val formatter = SimpleDateFormat(pattern, locale)
        return formatter.format(date)
    }
    
    /**
     * Format a timestamp as a date and time string with specified locale (legacy method)
     */
    fun formatDateTime(timestamp: Long, locale: Locale = Locale.getDefault()): String {
        val date = Date(timestamp)
        val pattern = "MMM d, yyyy HH:mm"
        val formatter = SimpleDateFormat(pattern, locale)
        return formatter.format(date)
    }
    
    /**
     * Format elapsed time in hours and minutes
     */
    fun formatElapsedTime(context: Context, elapsedTimeMillis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(elapsedTimeMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTimeMillis) % 60
        
        return if (hours > 0) {
            context.getString(R.string.format_hours_minutes, hours, minutes)
        } else {
            context.getString(R.string.format_minutes_only, minutes)
        }
    }
    
    /**
     * Format duration in a compact format
     */
    fun formatDuration(context: Context, durationMillis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(durationMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60
        
        return when {
            hours > 0 -> context.getString(R.string.format_hours_minutes, hours, minutes)
            else -> context.getString(R.string.format_minutes_only, minutes)
        }
    }
} 