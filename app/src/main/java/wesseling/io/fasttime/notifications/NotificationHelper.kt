package wesseling.io.fasttime.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.WearableExtender
import wesseling.io.fasttime.MainActivity
import wesseling.io.fasttime.R
import wesseling.io.fasttime.model.FastingState
import wesseling.io.fasttime.settings.PreferencesManager
import wesseling.io.fasttime.util.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Fasting State Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for fasting state changes"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Send a notification for a fasting state change
     */
    fun sendFastingStateNotification(fastingState: FastingState) {
        // Create an intent to open the app when the notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Generate a unique notification ID based on the fasting state
        // This ensures each state gets its own notification instead of replacing previous ones
        val notificationId = NOTIFICATION_ID_BASE + fastingState.ordinal
        
        // Get current time formatted according to user preferences
        val preferencesManager = PreferencesManager.getInstance(context)
        val preferences = preferencesManager.dateTimePreferences
        val currentTime = System.currentTimeMillis()
        val formattedTime = DateTimeFormatter.formatTime(currentTime, preferences)
        
        // Create the notification content
        val title = "New Fasting State: ${fastingState.displayName}"
        val shortText = "At $formattedTime, you reached the ${fastingState.displayName} state!"
        val longText = "At $formattedTime, you reached the ${fastingState.displayName} state: ${fastingState.description}"
        
        // Create a "View Details" action for wearables
        val viewDetailsIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(MainActivity.SHOW_FASTING_DETAILS, true)
        }
        
        val viewDetailsPendingIntent = PendingIntent.getActivity(
            context,
            1, // Different request code from the main intent
            viewDetailsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create a wearable extender for the notification
        val wearableExtender = WearableExtender()
            .setHintContentIntentLaunchesActivity(true)
            .setContentAction(0) // Set the first action as the main action
            .addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_play_arrow,
                    "View Details",
                    viewDetailsPendingIntent
                ).build()
            )
        
        // Build the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_play_arrow)
            .setContentTitle(title)
            .setContentText(shortText)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(longText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOngoing(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setWhen(currentTime) // Set the timestamp for the notification
            .setShowWhen(true) // Show the timestamp
            // Add action button for mobile devices
            .addAction(
                R.drawable.ic_play_arrow,
                "View Details",
                viewDetailsPendingIntent
            )
            // Add wearable features
            .extend(wearableExtender)
            .build()
        
        // Show the notification
        notificationManager.notify(notificationId, notification)
        
        // Vibrate the device to alert the user
        vibrateDevice(context)
    }
    
    private fun vibrateDevice(context: Context) {
        // Get vibrator service using the appropriate method based on Android version
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            // For older versions, use VibrationEffect.createOneShot with compatibility method
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }
    
    companion object {
        private const val CHANNEL_ID = "fasting_state_channel"
        private const val NOTIFICATION_ID_BASE = 1001
    }
} 