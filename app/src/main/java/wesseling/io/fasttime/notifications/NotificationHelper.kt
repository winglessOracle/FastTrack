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
        
        // Get fun and motivating content based on the fasting state
        val notificationContent = getMotivatingContent(fastingState, formattedTime)
        
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
                    "View Progress",
                    viewDetailsPendingIntent
                ).build()
            )
        
        // Build the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_play_arrow)
            .setContentTitle(notificationContent.title)
            .setContentText(notificationContent.shortText)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(notificationContent.longText))
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
                "View Progress",
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
    
    /**
     * Get fun and motivating notification content based on the fasting state
     */
    private fun getMotivatingContent(fastingState: FastingState, formattedTime: String): NotificationContent {
        return when (fastingState) {
            FastingState.NOT_FASTING -> {
                val titles = listOf(
                    "Fasting Journey Begins!",
                    "Ready, Set, Fast!",
                    "Your Fasting Adventure Starts!"
                )
                val shortTexts = listOf(
                    "You're in the Fed State - digestion in progress!",
                    "Fueled up and ready for your fasting journey!",
                    "Digestion mode activated at $formattedTime!"
                )
                val longTexts = listOf(
                    "Your body is currently processing nutrients. Soon, you'll transition to fat burning mode! Keep going, your cellular health journey is just beginning! 💪",
                    "You're in the Fed State (digestion & absorption). This is the perfect foundation for the amazing benefits that await as you continue your fast! 🚀",
                    "Digestion in progress! Your body is processing your last meal. Stick with it, and you'll soon unlock the powerful benefits of fasting! ✨"
                )
                randomContent(titles, shortTexts, longTexts)
            }
            FastingState.EARLY_FAST -> {
                val titles = listOf(
                    "Fat Burning Mode: Activated! 🔥",
                    "Level Up: Early Fasting Achieved! ⬆️",
                    "First Milestone Reached! 🎯"
                )
                val shortTexts = listOf(
                    "Your body is now switching to fat burning at $formattedTime!",
                    "4+ hours in - your metabolism is changing!",
                    "You've unlocked Early Fasting mode! Keep it up!"
                )
                val longTexts = listOf(
                    "Congratulations! At $formattedTime, your body started burning fat for energy! This is when the magic begins - insulin levels are dropping and fat burning is ramping up! 🔥",
                    "Amazing progress! You've reached Early Fasting state where your body begins to tap into fat stores. Your metabolism is thanking you! Keep going for even more benefits! 💯",
                    "4+ hours of fasting complete! Your body is transitioning from using glucose to burning fat. This is just the beginning of your fasting superpowers! 💪"
                )
                randomContent(titles, shortTexts, longTexts)
            }
            FastingState.GLYCOGEN_DEPLETION -> {
                val titles = listOf(
                    "Glycogen Reserves Depleting! 📉",
                    "Fat Burning Intensifies! 🔥🔥",
                    "12-Hour Milestone Crushed! 🏆"
                )
                val shortTexts = listOf(
                    "12+ hours in - fat metabolism increasing!",
                    "You're now in serious fat-burning territory!",
                    "Glycogen Depletion achieved at $formattedTime!"
                )
                val longTexts = listOf(
                    "Wow! At $formattedTime, your liver glycogen is depleting and fat metabolism is significantly increasing! Your body is becoming a fat-burning machine! 🔄",
                    "12+ hours of fasting - incredible work! Your body is now depleting glycogen stores and ramping up fat metabolism. This is where the real benefits begin! 🚀",
                    "You've reached Glycogen Depletion state! Your body is switching to fat as its primary fuel source. Keep going - you're unlocking amazing health benefits with every hour! ✨"
                )
                randomContent(titles, shortTexts, longTexts)
            }
            FastingState.METABOLIC_SHIFT -> {
                val titles = listOf(
                    "Ketosis Initiated! ⚡",
                    "Metabolic Magic Happening! ✨",
                    "18-Hour Superstar Status! 🌟"
                )
                val shortTexts = listOf(
                    "18+ hours - your body is entering ketosis!",
                    "Metabolic Shift achieved at $formattedTime!",
                    "You're now producing ketones for energy!"
                )
                val longTexts = listOf(
                    "Incredible achievement! At $formattedTime, your body entered the Metabolic Shift state where ketosis begins! Your brain is starting to use ketones for fuel - hello mental clarity! 🧠✨",
                    "18+ hours fasted - you're a fasting champion! Your metabolism has shifted to ketone production, offering enhanced focus and energy. Your cells are celebrating! 🎉",
                    "You've reached the Metabolic Shift milestone! Ketosis is beginning, bringing improved mental clarity and steady energy. Your body is thanking you for this amazing reset! 💫"
                )
                randomContent(titles, shortTexts, longTexts)
            }
            FastingState.DEEP_KETOSIS -> {
                val titles = listOf(
                    "Deep Ketosis Unlocked! 🔓",
                    "Autophagy Activated! 🧹",
                    "24-Hour Fasting Hero! 🦸"
                )
                val shortTexts = listOf(
                    "24+ hours - cellular cleanup in progress!",
                    "Deep Ketosis achieved at $formattedTime!",
                    "Autophagy is peaking - cellular renewal time!"
                )
                val longTexts = listOf(
                    "Phenomenal achievement! At $formattedTime, you entered Deep Ketosis where autophagy (cellular cleanup) peaks! Your body is removing damaged cells and creating new ones! 🧹✨",
                    "24+ hours of fasting - you're in the elite zone now! Deep Ketosis brings maximum autophagy, where your body cleans out damaged cellular components. You're literally renewing yourself! 🔄",
                    "Deep Ketosis achieved! Your body is now in a powerful state of cellular cleanup and renewal. This is where the transformative health benefits of fasting really shine! 💫"
                )
                randomContent(titles, shortTexts, longTexts)
            }
            FastingState.IMMUNE_RESET -> {
                val titles = listOf(
                    "Immune System Reboot! 🔄",
                    "Stem Cell Production Surge! 🌱",
                    "48-Hour Fasting Legend! 👑"
                )
                val shortTexts = listOf(
                    "48+ hours - stem cell production increasing!",
                    "Immune Reset achieved at $formattedTime!",
                    "Your immune system is regenerating!"
                )
                val longTexts = listOf(
                    "Extraordinary achievement! At $formattedTime, you reached the Immune Reset state! Your body is increasing stem cell production and regenerating your immune system! 🌱",
                    "48+ hours fasted - you've reached legendary status! Your body is now in Immune Reset mode, with increased stem cell production and significant immune system regeneration. Simply amazing! 🌟",
                    "Immune Reset unlocked! Your body is now producing more stem cells and regenerating your immune system. This is a profound level of healing that few experience! 💫"
                )
                randomContent(titles, shortTexts, longTexts)
            }
            FastingState.EXTENDED_FAST -> {
                val titles = listOf(
                    "Extended Fast Mastery! 🏆",
                    "Cellular Rejuvenation Maximized! ✨",
                    "72-Hour Fasting Champion! 👑"
                )
                val shortTexts = listOf(
                    "72+ hours - deep cellular rejuvenation!",
                    "Extended Fast achieved at $formattedTime!",
                    "Maximum rejuvenation mode activated!"
                )
                val longTexts = listOf(
                    "Extraordinary achievement! At $formattedTime, you entered the Extended Fast state! Your body is experiencing maximum cellular rejuvenation and profound healing! 🌟",
                    "72+ hours of fasting - you've reached the pinnacle! Extended Fasting brings the deepest level of cellular rejuvenation and metabolic benefits. You're among an elite few who reach this level! 👑",
                    "Extended Fast state achieved! You're experiencing the most profound benefits of fasting - deep cellular rejuvenation, maximum autophagy, and comprehensive metabolic reset. Truly remarkable! ✨"
                )
                randomContent(titles, shortTexts, longTexts)
            }
        }
    }
    
    /**
     * Randomly select content from the provided lists to keep notifications fresh and engaging
     */
    private fun randomContent(titles: List<String>, shortTexts: List<String>, longTexts: List<String>): NotificationContent {
        val randomIndex = Random.nextInt(titles.size)
        return NotificationContent(
            title = titles[randomIndex],
            shortText = shortTexts[randomIndex],
            longText = longTexts[randomIndex]
        )
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
        private const val NOTIFICATION_ID_BASE = 1001
    }
} 