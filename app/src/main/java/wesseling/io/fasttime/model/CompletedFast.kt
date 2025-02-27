package wesseling.io.fasttime.model

import android.content.Context
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import wesseling.io.fasttime.settings.PreferencesManager
import wesseling.io.fasttime.util.DateTimeFormatter

/**
 * Represents a completed fasting session
 */
data class CompletedFast(
    val id: String = UUID.randomUUID().toString(),
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val durationMillis: Long,
    val maxFastingState: FastingState,
    val note: String = ""
) {
    /**
     * Get the formatted start date and time
     */
    fun getFormattedStartTime(context: Context): String {
        val preferences = PreferencesManager.getInstance(context).dateTimePreferences
        return DateTimeFormatter.formatDateTime(startTimeMillis, preferences)
    }
    
    /**
     * Get the formatted end date and time
     */
    fun getFormattedEndTime(context: Context): String {
        val preferences = PreferencesManager.getInstance(context).dateTimePreferences
        return DateTimeFormatter.formatDateTime(endTimeMillis, preferences)
    }
    
    /**
     * Get the formatted duration in hours and minutes
     */
    fun getFormattedDuration(): String {
        val hours = durationMillis / (1000 * 60 * 60)
        val minutes = (durationMillis / (1000 * 60)) % 60
        
        return if (hours > 0) {
            "$hours h $minutes min"
        } else {
            "$minutes min"
        }
    }
    
    /**
     * For backward compatibility with code that doesn't pass context
     */
    fun getFormattedStartTime(): String {
        val dateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(startTimeMillis),
            ZoneId.systemDefault()
        )
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
    }
    
    /**
     * For backward compatibility with code that doesn't pass context
     */
    fun getFormattedEndTime(): String {
        val dateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(endTimeMillis),
            ZoneId.systemDefault()
        )
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
    }
} 