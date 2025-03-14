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
            // Add SHOW_FASTING_DETAILS to the main intent so tapping the notification
            // directly takes the user to the fasting log screen
            putExtra(MainActivity.SHOW_FASTING_DETAILS, true)
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
        
        // Create a wearable extender for the notification
        // Keep the "View Progress" action only for wearables where it's more useful
        val wearableExtender = WearableExtender()
            .setHintContentIntentLaunchesActivity(true)
            .setContentAction(0) // Set the first action as the main action
            .addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_play_arrow,
                    "View Progress",
                    pendingIntent
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
                    "Fasting Journey Begins! 🚀",
                    "Ready, Set, Fast! 🏁",
                    "Your Fasting Adventure Starts! 🌟"
                )
                val shortTexts = listOf(
                    "You're in the Fed State - digestion in progress!",
                    "Fueled up and ready for your fasting journey!",
                    "Digestion mode activated at $formattedTime!"
                )
                val longTexts = listOf(
                    "Fun Fact: Your body is currently releasing insulin to store nutrients. In a few hours, insulin will drop and your body will flip the metabolic switch! 💪",
                    "Did you know? During digestion, your body prioritizes storing energy rather than burning fat. Fasting reverses this process - you're on your way to fat-burning mode! 🔄",
                    "Science Bite: Right now your body is in 'storage mode' with high insulin levels. As you fast, insulin drops and growth hormone rises by up to 500%! ✨"
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
                    "Fun Fact: Your liver has now depleted about 1/3 of its glycogen (stored sugar). Your body is beginning to release fatty acids from fat cells for energy! 🔬",
                    "Science Bite: At this stage, your body is reducing insulin and increasing lipolysis - the breakdown of fat into usable energy. You're becoming a fat-burning machine! 🔋",
                    "Did you know? Your body is now producing ghrelin (hunger hormone) in waves that typically last only 20 minutes. Drink water and the wave will pass! 💧"
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
                    "Fun Fact: Your liver glycogen is now almost gone! Your body is increasing fat oxidation by 300% compared to the fed state. Hello, fat burning! 📊",
                    "Science Bite: Your body is now producing ketone bodies from fat. These ketones are super-efficient fuel for your brain and heart! 🧠❤️",
                    "Did you know? At this stage, your body is releasing norepinephrine, giving you natural energy and focus. It's your body's built-in energy drink! ⚡"
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
                    "Fun Fact: Your brain is now switching to ketones for about 25% of its energy needs. Many report enhanced mental clarity and focus at this stage! 🧠",
                    "Science Bite: Your insulin sensitivity is improving by the hour. This metabolic shift helps prevent type 2 diabetes and improves overall metabolic health! 📈",
                    "Did you know? The ketones you're producing act as signaling molecules that trigger anti-inflammatory pathways in your body. You're reducing inflammation with every hour! 🛡️"
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
                    "Fun Fact: Autophagy (cellular cleanup) is now in full swing! Your cells are recycling damaged components and creating new, healthier parts. It's like spring cleaning for your cells! 🧹",
                    "Science Bite: Your growth hormone levels have increased by up to 1300% by now, promoting muscle preservation and fat burning. You're in the biological fountain of youth! 🏋️",
                    "Did you know? The ketones you're producing are protecting your brain cells and may help prevent neurodegenerative diseases. Your brain is loving this fast! 🧠✨"
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
                    "Fun Fact: Your body is now breaking down old immune cells and generating new ones. This 'immune system reset' can help your body fight infections better! 🛡️",
                    "Science Bite: Stem cell production increases by 400% at this stage, helping to regenerate various tissues in your body. You're literally creating a newer version of yourself! 🌱",
                    "Did you know? Your body is now in a state of significant PKA inhibition, which triggers cellular protection mechanisms that can extend longevity. You're activating your longevity genes! ⏱️"
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
                    "Fun Fact: Your insulin sensitivity has improved by up to 70% by now! Your cells are super-responsive to insulin, which helps prevent diabetes and metabolic syndrome. 📉",
                    "Science Bite: BDNF (Brain-Derived Neurotrophic Factor) levels increase significantly at this stage, promoting the growth of new brain cells and protecting existing ones. Your brain is literally growing! 🧠",
                    "Did you know? At this stage, your body has activated AMPK pathways that promote cellular repair and longevity. You're experiencing one of the most profound anti-aging interventions known to science! ⏳"
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