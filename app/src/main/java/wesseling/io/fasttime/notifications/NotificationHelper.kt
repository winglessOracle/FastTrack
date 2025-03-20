package wesseling.io.fasttime.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import wesseling.io.fasttime.R
import wesseling.io.fasttime.model.FastingState
import wesseling.io.fasttime.settings.PreferencesManager
import wesseling.io.fasttime.util.DateTimeFormatter
import kotlin.random.Random

/**
 * Helper class for managing notifications
 */
class NotificationHelper(private val context: Context) {
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    init {
        createNotificationChannel()
    }
    
    /**
     * Create the notification channel for Android O and above
     */
    private fun createNotificationChannel() {
        // Since minSdk is 27 (Android 8.1), this code will always run
            val channel = NotificationChannel(
                CHANNEL_ID,
            context.getString(R.string.notification_channel_fasting_state),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
            description = context.getString(R.string.notification_channel_fasting_state_description)
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            
            notificationManager.createNotificationChannel(channel)
    }
    
    /**
     * Send a notification for a fasting state change
     */
    fun sendFastingStateNotification(fastingState: FastingState) {
        // Generate a unique notification ID based on the fasting state
        // This ensures each state gets its own notification instead of replacing previous ones
        val notificationId = NOTIFICATION_ID_BASE + fastingState.ordinal
        
        // Get current time formatted according to user preferences
        val preferencesManager = PreferencesManager.getInstance(context)
        val preferences = preferencesManager.dateTimePreferences
        val currentTime = System.currentTimeMillis()
        val formattedTime = DateTimeFormatter.formatTime(currentTime, preferences)
        
        // Get fun and motivating content based on the fasting state
        val notificationContent = getMotivatingContent(fastingState, formattedTime)
        
        // Build the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_play_arrow)
            .setContentTitle(notificationContent.title)
            .setContentText(notificationContent.shortText)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(notificationContent.longText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setWhen(currentTime) // Set the timestamp for the notification
            .setShowWhen(true) // Show the timestamp
            .build()
        
        // Show the notification
        notificationManager.notify(notificationId, notification)
        
        // Vibrate the device to alert the user
        vibrateDevice(context)
    }
    
    /**
     * Get fun and motivating notification content based on the fasting state
     */
    private fun getMotivatingContent(fastingState: FastingState, formattedTime: String): NotificationContent {
        return when (fastingState) {
            FastingState.NOT_FASTING -> {
                // Use string resources with random selection for variety
                val titleResIds = listOf(
                    R.string.notification_title_not_fasting_1,
                    R.string.notification_title_not_fasting_2,
                    R.string.notification_title_not_fasting_3,
                    R.string.notification_title_not_fasting_4
                )
                
                val shortTextResIds = listOf(
                    R.string.notification_short_not_fasting_1,
                    R.string.notification_short_not_fasting_2,
                    R.string.notification_short_not_fasting_3,
                    R.string.notification_short_not_fasting_4
                )
                
                // Pick random items
                val titleResId = titleResIds.random()
                val shortTextResId = shortTextResIds.random()
                
                // Get the strings from resources
                val title = context.getString(titleResId)
                val shortText = context.getString(shortTextResId, formattedTime)
                
                // For long text, we'll use a simple format for now
                val longText = context.getString(
                    R.string.notification_format_long_text,
                    shortText,
                    "You're at the start of your fasting journey. As you continue fasting, your body will begin to tap into fat stores for energy."
                )
                
                NotificationContent(title, shortText, longText)
            }
            
            FastingState.EARLY_FAST -> {
                // Use string resources with random selection for variety
                val titleResIds = listOf(
                    R.string.notification_title_early_fast_1,
                    R.string.notification_title_early_fast_2,
                    R.string.notification_title_early_fast_3,
                    R.string.notification_title_early_fast_4
                )
                
                val shortTextResIds = listOf(
                    R.string.notification_short_early_fast_1,
                    R.string.notification_short_early_fast_2,
                    R.string.notification_short_early_fast_3,
                    R.string.notification_short_early_fast_4
                )
                
                // Pick random items
                val titleResId = titleResIds.random()
                val shortTextResId = shortTextResIds.random()
                
                // Get the strings from resources
                val title = context.getString(titleResId)
                val shortText = context.getString(shortTextResId, formattedTime)
                
                // For long text, we'll use a simple format for now
                val longText = context.getString(
                    R.string.notification_format_long_text,
                    shortText,
                    "Your body is now starting to use fat as fuel. As insulin drops, your body releases fatty acids from fat cells."
                )
                
                NotificationContent(title, shortText, longText)
            }
            
            // For other states, we'll keep the existing implementation for now
            // In a complete implementation, all these would use string resources
            else -> {
                // Get the appropriate resource IDs based on the fasting state
                val titleResIds = when (fastingState) {
                    FastingState.GLYCOGEN_DEPLETION -> listOf(
                        R.string.notification_title_glycogen_depletion_1,
                        R.string.notification_title_glycogen_depletion_2,
                        R.string.notification_title_glycogen_depletion_3,
                        R.string.notification_title_glycogen_depletion_4
                    )
                    FastingState.METABOLIC_SHIFT -> listOf(
                        R.string.notification_title_metabolic_shift_1,
                        R.string.notification_title_metabolic_shift_2,
                        R.string.notification_title_metabolic_shift_3,
                        R.string.notification_title_metabolic_shift_4
                    )
                    FastingState.DEEP_KETOSIS -> listOf(
                        R.string.notification_title_deep_ketosis_1,
                        R.string.notification_title_deep_ketosis_2,
                        R.string.notification_title_deep_ketosis_3,
                        R.string.notification_title_deep_ketosis_4
                    )
                    FastingState.IMMUNE_RESET -> listOf(
                        R.string.notification_title_immune_reset_1,
                        R.string.notification_title_immune_reset_2,
                        R.string.notification_title_immune_reset_3,
                        R.string.notification_title_immune_reset_4
                    )
                    FastingState.EXTENDED_FAST -> listOf(
                        R.string.notification_title_extended_fast_1,
                        R.string.notification_title_extended_fast_2,
                        R.string.notification_title_extended_fast_3,
                        R.string.notification_title_extended_fast_4
                    )
                    else -> listOf(R.string.notification_title_not_fasting_1) // Fallback
                }
                
                val shortTextResIds = when (fastingState) {
                    FastingState.GLYCOGEN_DEPLETION -> listOf(
                        R.string.notification_short_glycogen_depletion_1,
                        R.string.notification_short_glycogen_depletion_2,
                        R.string.notification_short_glycogen_depletion_3,
                        R.string.notification_short_glycogen_depletion_4
                    )
                    FastingState.METABOLIC_SHIFT -> listOf(
                        R.string.notification_short_metabolic_shift_1,
                        R.string.notification_short_metabolic_shift_2,
                        R.string.notification_short_metabolic_shift_3,
                        R.string.notification_short_metabolic_shift_4
                    )
                    FastingState.DEEP_KETOSIS -> listOf(
                        R.string.notification_short_deep_ketosis_1,
                        R.string.notification_short_deep_ketosis_2,
                        R.string.notification_short_deep_ketosis_3,
                        R.string.notification_short_deep_ketosis_4
                    )
                    FastingState.IMMUNE_RESET -> listOf(
                        R.string.notification_short_immune_reset_1,
                        R.string.notification_short_immune_reset_2,
                        R.string.notification_short_immune_reset_3,
                        R.string.notification_short_immune_reset_4
                    )
                    FastingState.EXTENDED_FAST -> listOf(
                        R.string.notification_short_extended_fast_1,
                        R.string.notification_short_extended_fast_2,
                        R.string.notification_short_extended_fast_3,
                        R.string.notification_short_extended_fast_4
                    )
                    else -> listOf(R.string.notification_short_not_fasting_1) // Fallback
                }
                
                // Pick random items
                val titleResId = titleResIds.random()
                val shortTextResId = shortTextResIds.random()
                
                // Get the strings from resources
                val title = context.getString(titleResId)
                val shortText = context.getString(shortTextResId, formattedTime)
                
                // Create long text from short text plus a detailed explanation
                // This could also be localized in the future, but for now we'll keep the current implementation
                val additionalInfo = when (fastingState) {
                    FastingState.GLYCOGEN_DEPLETION -> 
                        "Your body has now depleted glycogen stores and shifted into a primarily fat-burning state. This is where the metabolic magic happens!"
                    FastingState.METABOLIC_SHIFT -> 
                        "Your liver is producing significant ketones now. Many people report increased mental clarity during this phase!"
                    FastingState.DEEP_KETOSIS -> 
                        "Autophagy (cellular cleanup) is significantly elevated. Your cells are recycling damaged components and proteins."
                    FastingState.IMMUNE_RESET -> 
                        "Your body is breaking down older immune cells while creating space for new ones when you break your fast!"
                    FastingState.EXTENDED_FAST -> 
                        "You've reached peak autophagy and activated powerful healing mechanisms throughout your body!"
                    else -> ""
                }
                
                // For long text, use a simple format
                val longText = context.getString(
                    R.string.notification_format_long_text,
                    shortText,
                    additionalInfo
                )
                
                NotificationContent(title, shortText, longText)
            }
        }
    }
    
    /**
     * Vibrate the device to alert the user
     */
    private fun vibrateDevice(context: Context) {
        try {
            // Get vibrator service based on Android version
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
            // Vibrate pattern: 0ms delay, 500ms vibrate, 200ms pause, 500ms vibrate
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Use VibrationEffect for Android O and above
                val vibrationEffect = VibrationEffect.createWaveform(
                    longArrayOf(0, 500, 200, 500),
                    -1 // Don't repeat
                )
                vibrator.vibrate(vibrationEffect)
        } else {
                // Use deprecated method for older versions
            @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 500, 200, 500), -1)
            }
        } catch (e: Exception) {
            // Ignore errors, vibration is not critical
        }
    }
    
    /**
     * Data class to hold notification content
     */
    private data class NotificationContent(
        val title: String,
        val shortText: String,
        val longText: String
    )
    
    companion object {
        private const val CHANNEL_ID = "fasting_state_channel"
        private const val NOTIFICATION_ID_BASE = 1000
    }
} 