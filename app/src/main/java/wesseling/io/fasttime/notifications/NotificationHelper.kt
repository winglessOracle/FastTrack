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
                val titles = when (fastingState) {
                    FastingState.GLYCOGEN_DEPLETION -> listOf(
                        "Glycogen Depleted! 🔄",
                        "Fat Burning Optimized! 🔥",
                        "12-Hour Milestone Reached! 🏆",
                        "Metabolic Magic Happening! ✨",
                        "Insulin Levels Minimized! 📉",
                        "Glucose Reserves Depleted! 🧬",
                        "Fat Metabolism Maximized! 📈",
                        "Glycogen to Fat Switch Complete! 🔄"
                    )
                    FastingState.METABOLIC_SHIFT -> listOf(
                        "Ketones Detected! ⚡",
                        "Metabolic Shift Achieved! 🔄",
                        "18-Hour Milestone Reached! 🏆",
                        "Ketosis Has Begun! 🔑",
                        "Brain Fuel Switching! 🧠",
                        "Cellular Cleanup Initiated! 🧹",
                        "Body Optimization Mode! 🚀",
                        "Ketone Production Rising! 📈"
                    )
                    FastingState.DEEP_KETOSIS -> listOf(
                        "Deep Ketosis Unlocked! 🔓",
                        "Autophagy Activated! 🧬",
                        "24-Hour Milestone Crushed! 🏆",
                        "Cellular Cleanup Maximal! 🧹",
                        "Peak Fat Adaptation! 🔝",
                        "Ketone Levels Optimized! ⚡",
                        "Metabolic Powerhouse Mode! 💪",
                        "Cellular Renewal Engaged! 🔄"
                    )
                    FastingState.IMMUNE_RESET -> listOf(
                        "Immune System Reset! 🛡️",
                        "Stem Cell Production Up! 🌱",
                        "48-Hour Achievement Unlocked! 🏆",
                        "Growth Hormone Maximized! 📈",
                        "Immune Healing Activated! ✨",
                        "Cellular Rejuvenation Peak! 🔄",
                        "Profound Healing Mode! 💖",
                        "Regeneration Supercharged! ⚡"
                    )
                    FastingState.EXTENDED_FAST -> listOf(
                        "Extended Fast Master! 👑",
                        "Cellular Rejuvenation Complete! ✨",
                        "72-Hour Elite Status Achieved! 🏆",
                        "Maximum Autophagy Unlocked! 🔑",
                        "Metabolic Reset Complete! 🔄",
                        "Ultimate Fasting Level! 🔝",
                        "Peak Healing State! 💫",
                        "Fasting Virtuoso Mode! 🌟"
                    )
                    else -> listOf("Fasting Update") // Fallback (shouldn't be needed)
                }
                
                val shortTexts = when (fastingState) {
                    FastingState.GLYCOGEN_DEPLETION -> listOf(
                        "12+ hours in! Your liver glycogen is depleted at $formattedTime!",
                        "You've entered the fat-burning zone!",
                        "Insulin is minimized - fat cells are fully open for business!",
                        "12-hour milestone reached! Your body is now primarily burning fat!",
                        "Glycogen stores depleted - full fat-burning mode activated!",
                        "You've unlocked enhanced fat metabolism!",
                        "Congratulations on 12+ hours! Major metabolic benefits happening now!",
                        "Milestone achieved: Your body is now in primary fat-burning mode!"
                    )
                    FastingState.METABOLIC_SHIFT -> listOf(
                        "18+ hours in! Ketosis beginning at $formattedTime!",
                        "Metabolic shift achieved! Your brain is starting to use ketones!",
                        "18-hour milestone reached! Cellular cleanup is ramping up!",
                        "Ketones rising - your brain's alternative fuel system is activating!",
                        "Autophagy is increasing - cellular cleanup in progress!",
                        "You've unlocked the metabolic shift phase!",
                        "Major milestone: Your liver is now producing significant ketones!",
                        "Congratulations on 18+ hours! Your metabolism is transforming!"
                    )
                    FastingState.DEEP_KETOSIS -> listOf(
                        "24+ hours in! Deep ketosis achieved at $formattedTime!",
                        "Full day of fasting complete! Autophagy is peaking!",
                        "24-hour milestone reached! Maximum cellular benefits!",
                        "Deep ketosis unlocked - your body is fully fat-adapted!",
                        "Autophagy peaked - cellular cleanup is maximized!",
                        "Impressive achievement: 24+ hours of fasting!",
                        "Full ketosis established - your metabolism is transformed!",
                        "Major milestone reached: You've completed a full day of fasting!"
                    )
                    FastingState.IMMUNE_RESET -> listOf(
                        "48+ hours in! Immune system reset at $formattedTime!",
                        "Two-day milestone reached! Stem cell production increased!",
                        "48-hour achievement unlocked! Profound healing in progress!",
                        "Immune reset phase - your body is regenerating immune cells!",
                        "Growth hormone at peak levels - tissue repair maximized!",
                        "Incredible achievement: You've fasted for 48+ hours!",
                        "Major milestone: Your immune system is regenerating!",
                        "Two full days of fasting - extraordinary health benefits activated!"
                    )
                    FastingState.EXTENDED_FAST -> listOf(
                        "72+ hours in! Extended fast mastery at $formattedTime!",
                        "Three-day milestone achieved! Complete cellular rejuvenation!",
                        "72-hour elite status unlocked! Maximum autophagy sustained!",
                        "Extended fast mastered - profound metabolic healing achieved!",
                        "Maximum autophagy sustained - deep cellular cleansing complete!",
                        "Exceptional achievement: 72+ hours of fasting!",
                        "Elite fasting status reached - profound healing activated!",
                        "Three full days fasted - extraordinary willpower and health benefits!"
                    )
                    else -> listOf("Your fast is continuing.") // Fallback (shouldn't be needed)
                }
                
                val longTexts = when (fastingState) {
                    FastingState.GLYCOGEN_DEPLETION -> listOf(
                        "Science Bite: Your liver has now depleted its glycogen stores (about 100g), causing a significant increase in fat oxidation. Your body is now primarily burning fat for fuel! 🔬",
                        "Did you know? At this stage, your insulin levels are at their lowest point in the day. This maximizes fat burning and begins to trigger cellular cleanup processes! 📊",
                        "Fun Fact: When glycogen is depleted, your body releases twice as many fatty acids from fat stores. These fatty acids travel to the liver where they're converted to ketone bodies for energy! 🧪",
                        "Educational Moment: You've now entered a more significant fat-burning state. Your body is producing ketones at a low level, which will gradually increase. These ketones provide an alternative fuel source for your brain and body! 🧠",
                        "Motivation: You've hit a major milestone! At 12+ hours of fasting, you've depleted your glycogen stores and shifted into a primarily fat-burning state. This is where the metabolic magic happens! 🌟",
                        "Science Bite: Your body now has significantly reduced insulin levels, which 'unlocks' fat cells allowing them to release their stored fatty acids as your primary fuel source. 🔑",
                        "Fun Fact: If you exercise during this phase of fasting, you can further accelerate fat burning! Even a 20-30 minute walk can significantly boost your results. 🚶",
                        "Did you know? At this stage, human growth hormone (HGH) has increased by about 300%, helping preserve muscle mass while your body burns fat! 💪"
                    )
                    FastingState.METABOLIC_SHIFT -> listOf(
                        "Science Bite: Your liver is now producing significant amounts of ketones - specifically beta-hydroxybutyrate (BHB) and acetoacetate. These ketones are providing up to 25% of your brain's energy needs! 🧪",
                        "Educational Moment: At 18+ hours, autophagy (cellular cleanup) is increasing dramatically. This process removes damaged cellular components and helps recycle proteins, potentially reducing risk for various diseases! 🧹",
                        "Did you know? Your brain normally relies on glucose, but it's now making the switch to using ketones for up to 70% of its energy needs! This metabolic flexibility is a remarkable evolutionary adaptation! 🧠",
                        "Fun Fact: Ketones are actually a more efficient energy source than glucose, producing more ATP (cellular energy) per molecule. Many people report increased mental clarity during this phase! ⚡",
                        "Motivation: You've reached a significant metabolic milestone! Your body is now strongly in fat-burning mode and starting to ramp up ketone production. This is when many people report feeling a 'second wind' of energy! 🌬️",
                        "Science Bite: Fasting researcher Dr. Valter Longo has shown that this metabolic shift to ketosis helps your body clear out damaged cells and create new healthy ones - like pressing a metabolic reset button! 🔄",
                        "Fun Fact: At this stage, your insulin sensitivity has already improved significantly, which helps your body manage blood sugar more effectively even after your fast ends! 📉",
                        "Did you know? The metabolic shift you're experiencing now mimics what our ancestors regularly experienced, helping to explain why our bodies are so well-adapted to cycles of fasting! 🌍"
                    )
                    FastingState.DEEP_KETOSIS -> listOf(
                        "Science Bite: After 24 hours of fasting, autophagy (cellular cleanup) is significantly elevated. Your cells are busy recycling damaged components and proteins, potentially helping to reduce the risk of neurodegenerative diseases! 🧠",
                        "Fun Fact: Your ketone levels have now increased by up to 30 times compared to your fed state! These ketones are not just fuel - they're signaling molecules that trigger anti-inflammatory and cell-protective pathways! ⚡",
                        "Educational Moment: At this stage, your body has fully adapted to using fat for fuel. Studies show that after 24 hours of fasting, fat oxidation increases by up to 300%! 🔥",
                        "Did you know? One full day of fasting can trigger a 1300-2000% increase in human growth hormone! This helps preserve muscle and bone mass while promoting fat loss. 💪",
                        "Motivation: Completing 24 hours is a major achievement! You've given your digestive system a complete rest and activated profound cellular renewal processes. Your metabolic flexibility is impressive! 🌟",
                        "Science Bite: Research from the University of Southern California suggests that a 24-hour fast can help rejuvenate immune cells by triggering stem cell-based regeneration! 🧫",
                        "Fun Fact: Your body's inflammatory markers have likely dropped significantly at this point, potentially helping to reduce chronic inflammation - a root cause of many modern diseases! 🔬",
                        "Did you know? The ketones your body is producing are now providing up to 70% of your brain's energy needs, and many people report enhanced focus and cognitive clarity at this stage! 🧠"
                    )
                    FastingState.IMMUNE_RESET -> listOf(
                        "Science Bite: A 48-hour fast triggers a process called 'autophagy of the immune cells.' Your body is breaking down and recycling older, damaged immune cells while creating space for new, healthy ones! 🧫",
                        "Fun Fact: Your body is now activating 'PKA signaling pathways,' which researchers at USC found can trigger stem cell-based regeneration of new immune cells when you refeed! 🌱",
                        "Educational Moment: At this stage, your insulin levels have been minimal for an extended period, significantly improving insulin sensitivity. Studies show this effect can last for weeks after fasting! 📊",
                        "Did you know? After 48 hours of fasting, your body increases the production of brain-derived neurotrophic factor (BDNF), which promotes brain health and may help protect against neurodegenerative diseases! 🧠",
                        "Motivation: Completing 48 hours of fasting is an extraordinary achievement! You've activated profound healing mechanisms that few people in modern society ever experience. Your dedication is remarkable! 🌟",
                        "Science Bite: Research at Harvard University has shown that extensive fasting periods like yours can activate sirtuins - proteins that regulate cellular health and longevity! 🧬",
                        "Fun Fact: Your body's ketone production is now fully optimized! These ketones are activating the HDAC inhibitor pathway, which regulates gene expression and helps protect your DNA! 🔬",
                        "Did you know? By this point, your insulin-like growth factor (IGF-1) has dropped significantly - a change associated with reduced risk of certain diseases and potential longevity benefits! ⏱️"
                    )
                    FastingState.EXTENDED_FAST -> listOf(
                        "Science Bite: A 72-hour fast represents peak autophagy (cellular cleanup). Your body is efficiently recycling damaged cellular components and proteins while activating stress resistance pathways that promote longevity! 🧬",
                        "Fun Fact: At this stage, researchers at the Longevity Institute have observed significant regeneration of the immune system upon refeeding, with the body producing new white blood cells from hematopoietic stem cells! 🌱",
                        "Educational Moment: After 72 hours, your body has maximized ketone production and fat adaptation. These metabolic changes help preserve muscle tissue while continuing to burn fat stores for energy! 💪",
                        "Did you know? Extended fasting activates a process called 'xenophagy,' which helps your cells identify and destroy pathogens that might otherwise go undetected! 🔬",
                        "Motivation: Completing 72 hours is an elite achievement that few people in modern society ever experience! You've demonstrated remarkable determination and have activated powerful healing mechanisms throughout your body. 🏆",
                        "Science Bite: A study in Cell Stem Cell showed that fasting for 72+ hours promotes stem cell self-renewal in multiple tissues, potentially rejuvenating your entire body! 🌟",
                        "Fun Fact: By this point, your body has dramatically reduced inflammation throughout all systems. Many long-term fasters report improvements in inflammatory conditions after an extended fast like yours! 🔥",
                        "Did you know? At this stage, your body has optimized all systems for survival without food intake - a remarkable evolutionary adaptation that few modern humans ever experience! 🌍"
                    )
                    else -> listOf("Your fast is continuing.") // Fallback (shouldn't be needed)
                }
                
                // Pick random content
                val titleIndex = Random.nextInt(titles.size)
                val textIndex = Random.nextInt(shortTexts.size)
                val longTextIndex = Random.nextInt(longTexts.size)
                
                NotificationContent(
                    titles[titleIndex],
                    shortTexts[textIndex],
                    longTexts[longTextIndex]
                )
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