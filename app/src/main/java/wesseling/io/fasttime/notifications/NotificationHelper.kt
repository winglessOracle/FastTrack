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
                val titles = listOf(
                    "Fasting Journey Begins! 🚀",
                    "Ready, Set, Fast! 🏁",
                    "Your Fasting Adventure Starts! 🌟",
                    "Digestion in Progress! 🍽️",
                    "Fasting Countdown Started! ⏱️",
                    "Nutrient Absorption Time! 🥗",
                    "Fasting Mode: Loading... ⌛",
                    "Metabolism Reset Initiated! 🔄"
                )
                val shortTexts = listOf(
                    "You're in the Fed State - digestion in progress!",
                    "Fueled up and ready for your fasting journey!",
                    "Digestion mode activated at $formattedTime!",
                    "Insulin at work - storing nutrients now!",
                    "Prep phase: Your body's getting ready to fast!",
                    "Fasting benefits coming soon!",
                    "Digestion now, fat burning later!",
                    "Fed state: The calm before the metabolic storm!"
                )
                val longTexts = listOf(
                    "Fun Fact: Your body is currently releasing insulin to store nutrients. In a few hours, insulin will drop and your body will flip the metabolic switch! 💪",
                    "Did you know? During digestion, your body prioritizes storing energy rather than burning fat. Fasting reverses this process - you're on your way to fat-burning mode! 🔄",
                    "Science Bite: Right now your body is in 'storage mode' with high insulin levels. As you fast, insulin drops and growth hormone rises by up to 500%! ✨",
                    "Educational Moment: In the fed state, your body's main fuel is glucose from your last meal. Once that's used up, you'll start tapping into stored energy. Patience pays off! 🧬",
                    "Motivation: Every fasting journey begins with a single meal skipped. You're laying the foundation for metabolic flexibility and better health! 🌱",
                    "Fun Fact: Your digestive system is working hard right now! It takes about 6-8 hours to fully process a meal. After that, the real magic of fasting begins! ⭐",
                    "Science Bite: During digestion, blood flow increases to your digestive organs by up to 30%. Once digestion is complete, that energy can be redirected to repair and rejuvenation! 🔬",
                    "Humor Break: Your body right now: 'Ooh, calories! Let me store these for later!' Your body in a few hours: 'Wait, where's breakfast? Fine, I'll burn some fat instead!' 😄"
                )
                randomContent(titles, shortTexts, longTexts)
            }
            FastingState.EARLY_FAST -> {
                val titles = listOf(
                    "Fat Burning Mode: Activated! 🔥",
                    "Level Up: Early Fasting Achieved! ⬆️",
                    "First Milestone Reached! 🎯",
                    "Insulin Levels Dropping! 📉",
                    "Metabolism Shifting Gears! ⚙️",
                    "Fat Cells: Now Open for Business! 💼",
                    "Fasting Power-Up Unlocked! 🎮",
                    "Operation Fat Burn: Initiated! 🚀"
                )
                val shortTexts = listOf(
                    "Your body is now switching to fat burning at $formattedTime!",
                    "4+ hours in - your metabolism is changing!",
                    "You've unlocked Early Fasting mode! Keep it up!",
                    "Insulin dropping, fat burning rising!",
                    "First fat-burning milestone reached!",
                    "Goodbye glucose, hello fatty acids!",
                    "Early fast stage: Your body's warming up!",
                    "Fat cells are starting to release energy!"
                )
                val longTexts = listOf(
                    "Fun Fact: Your liver has now depleted about 1/3 of its glycogen (stored sugar). Your body is beginning to release fatty acids from fat cells for energy! 🔬",
                    "Science Bite: At this stage, your body is reducing insulin and increasing lipolysis - the breakdown of fat into usable energy. You're becoming a fat-burning machine! 🔋",
                    "Did you know? Your body is now producing ghrelin (hunger hormone) in waves that typically last only 20 minutes. Drink water and the wave will pass! 💧",
                    "Educational Moment: Your body has multiple fuel tanks - first it burns glucose in the blood, then liver glycogen, then fat. You're transitioning to the fat-burning phase now! 🧪",
                    "Motivation: This is when willpower matters most! Push through the initial hunger pangs, and your body will adapt to burning fat more efficiently. You've got this! 💪",
                    "Fun Fact: During early fasting, your body increases production of norepinephrine and epinephrine, giving you a natural energy boost and mental clarity. Fasting superpower activated! ⚡",
                    "Science Bite: The drop in insulin during this phase helps activate AMPK, a cellular energy sensor that promotes fat burning and blocks fat storage. Your metabolism is getting smarter! 🧠",
                    "Humor Break: Your fat cells right now: 'Wait, we're actually being used for something? We thought we were just permanent residents!' 😂"
                )
                randomContent(titles, shortTexts, longTexts)
            }
            FastingState.GLYCOGEN_DEPLETION -> {
                val titles = listOf(
                    "Glycogen Reserves Depleting! 📉",
                    "Fat Burning Intensifies! 🔥🔥",
                    "12-Hour Milestone Crushed! 🏆",
                    "Sugar Stores Empty, Fat Burning Full! 🔄",
                    "Metabolic Switch: Flipped! 🔌",
                    "Fat-Burning Zone: Entered! 🚪",
                    "Glycogen Gone, Ketones Rising! 📈",
                    "Cellular Cleanup Crew: Assembling! 🧹"
                )
                val shortTexts = listOf(
                    "12+ hours in - fat metabolism increasing!",
                    "You're now in serious fat-burning territory!",
                    "Glycogen Depletion achieved at $formattedTime!",
                    "Liver glycogen nearly gone - fat burning ramping up!",
                    "Ketone production starting now!",
                    "Your body is now primarily burning fat!",
                    "12hr mark: Metabolic magic happening!",
                    "Fat oxidation up 300% - you're on fire (metabolically)!"
                )
                val longTexts = listOf(
                    "Fun Fact: Your liver glycogen is now almost gone! Your body is increasing fat oxidation by 300% compared to the fed state. Hello, fat burning! 📊",
                    "Science Bite: Your body is now producing ketone bodies from fat. These ketones are super-efficient fuel for your brain and heart! 🧠❤️",
                    "Did you know? At this stage, your body is releasing norepinephrine, giving you natural energy and focus. It's your body's built-in energy drink! ⚡",
                    "Educational Moment: Your liver can only store about 100g of glycogen (sugar), which lasts about 12-16 hours. Now that it's depleted, your body is switching to its virtually unlimited fat stores! 🏭",
                    "Motivation: You've made it past the hardest part! Many people never reach this stage of fasting, but you've pushed through. Your metabolism is thanking you! 🙏",
                    "Fun Fact: The ketones your body is now producing have anti-inflammatory properties and provide more energy per unit of oxygen than glucose. You're becoming a more efficient machine! ⚙️",
                    "Science Bite: During glycogen depletion, your body activates SIRT1 genes, which are associated with longevity and cellular health. You're literally turning on your longevity genes! 🧬",
                    "Humor Break: Your body right now: 'No more easy glucose? Fine, I'll use these fat stores I've been saving for a special occasion. I guess this is special enough!' 🤣"
                )
                randomContent(titles, shortTexts, longTexts)
            }
            FastingState.METABOLIC_SHIFT -> {
                val titles = listOf(
                    "Ketosis Initiated! ⚡",
                    "Metabolic Magic Happening! ✨",
                    "18-Hour Superstar Status! 🌟",
                    "Brain Switching to Ketones! 🧠",
                    "Anti-Inflammatory Mode: On! 🛡️",
                    "Ketone Levels Rising! 📈",
                    "Metabolic Flexibility Achievement! 🏅",
                    "Fat-Burning Turbo Mode: Engaged! 🚀"
                )
                val shortTexts = listOf(
                    "18+ hours - your body is entering ketosis!",
                    "Metabolic Shift achieved at $formattedTime!",
                    "You're now producing ketones for energy!",
                    "Brain fog lifting? That's ketones at work!",
                    "Inflammation markers dropping now!",
                    "18hr mark: Your metabolism is transforming!",
                    "Ketones now fueling your brain and body!",
                    "Feeling focused? Thank your ketones!"
                )
                val longTexts = listOf(
                    "Fun Fact: Your brain is now switching to ketones for about 25% of its energy needs. Many report enhanced mental clarity and focus at this stage! 🧠",
                    "Science Bite: Your insulin sensitivity is improving by the hour. This metabolic shift helps prevent type 2 diabetes and improves overall metabolic health! 📈",
                    "Did you know? The ketones you're producing act as signaling molecules that trigger anti-inflammatory pathways in your body. You're reducing inflammation with every hour! 🛡️",
                    "Educational Moment: Ketones aren't just fuel - they're signaling molecules that regulate gene expression, reduce oxidative stress, and promote brain health. Your body is getting a complete upgrade! 🔬",
                    "Motivation: You're now experiencing what our ancestors felt regularly. This metabolic state helped humans survive food scarcity and might be our natural optimized state! 🌿",
                    "Fun Fact: The ketone body beta-hydroxybutyrate (BHB) is not just a fuel but also a signaling molecule that can regulate the expression of genes related to stress resistance! 🧪",
                    "Science Bite: At this stage, your body is upregulating the production of antioxidant enzymes like catalase and glutathione, boosting your cellular defenses against oxidative damage! 🛡️",
                    "Humor Break: Your brain cells right now: 'These ketones are amazing! Why have we been stuck using glucose all this time? This is premium fuel!' 😄"
                )
                randomContent(titles, shortTexts, longTexts)
            }
            FastingState.DEEP_KETOSIS -> {
                val titles = listOf(
                    "Deep Ketosis Unlocked! 🔓",
                    "Autophagy Activated! 🧹",
                    "24-Hour Fasting Hero! 🦸",
                    "Cellular Cleanup in Full Swing! 🧽",
                    "Growth Hormone Surging! 💪",
                    "Autophagy Level: Maximum! ⬆️",
                    "Cellular Renewal Activated! 🔄",
                    "24hr Achievement Unlocked! 🏆"
                )
                val shortTexts = listOf(
                    "24+ hours - cellular cleanup in progress!",
                    "Deep Ketosis achieved at $formattedTime!",
                    "Autophagy is peaking - cellular renewal time!",
                    "Damaged cells being recycled now!",
                    "Growth hormone up 1300% - youth mode on!",
                    "24hr mark: Your cells are getting a deep clean!",
                    "Brain-protecting ketones at peak levels!",
                    "You're in the cellular rejuvenation zone!"
                )
                val longTexts = listOf(
                    "Fun Fact: Autophagy (cellular cleanup) is now in full swing! Your cells are recycling damaged components and creating new, healthier parts. It's like spring cleaning for your cells! 🧹",
                    "Science Bite: Your growth hormone levels have increased by up to 1300% by now, promoting muscle preservation and fat burning. You're in the biological fountain of youth! 🏋️",
                    "Did you know? The ketones you're producing are protecting your brain cells and may help prevent neurodegenerative diseases. Your brain is loving this fast! 🧠✨",
                    "Educational Moment: Autophagy (meaning 'self-eating') is your body's way of removing damaged cellular components. This process, which peaks around 24 hours of fasting, won the Nobel Prize in Medicine in 2016! 🏅",
                    "Motivation: You've completed a full day of fasting! This is a significant milestone that few people reach. Your body is rewarding you with enhanced cellular repair and rejuvenation! 🎉",
                    "Fun Fact: During deep ketosis, your body produces more BDNF (Brain-Derived Neurotrophic Factor), which acts like fertilizer for your brain, promoting neural growth and protection! 🌱",
                    "Science Bite: At this stage, your body is breaking down misfolded proteins that can contribute to neurodegenerative diseases. You're literally cleaning out potential disease-causing debris! 🧬",
                    "Humor Break: Your cells right now: 'Time for extreme makeover: cellular edition! Out with the old damaged parts, in with the new efficient components!' 🛠️"
                )
                randomContent(titles, shortTexts, longTexts)
            }
            FastingState.IMMUNE_RESET -> {
                val titles = listOf(
                    "Immune System Reboot! 🔄",
                    "Stem Cell Production Surge! 🌱",
                    "48-Hour Fasting Legend! 👑",
                    "Immune System: Regenerating! 🛡️",
                    "Stem Cell Factory: Open! 🏭",
                    "Cellular Defense Upgrade! ⚔️",
                    "Longevity Pathways Activated! ⏳",
                    "48hr Superhuman Status Achieved! 💯"
                )
                val shortTexts = listOf(
                    "48+ hours - stem cell production increasing!",
                    "Immune Reset achieved at $formattedTime!",
                    "Your immune system is regenerating!",
                    "Old immune cells out, new ones in!",
                    "Stem cell production up 400%!",
                    "48hr mark: Your body is rebuilding itself!",
                    "Longevity genes fully activated now!",
                    "You've reached elite fasting territory!"
                )
                val longTexts = listOf(
                    "Fun Fact: Your body is now breaking down old immune cells and generating new ones. This 'immune system reset' can help your body fight infections better! 🛡️",
                    "Science Bite: Stem cell production increases by 400% at this stage, helping to regenerate various tissues in your body. You're literally creating a newer version of yourself! 🌱",
                    "Did you know? Your body is now in a state of significant PKA inhibition, which triggers cellular protection mechanisms that can extend longevity. You're activating your longevity genes! ⏱️",
                    "Educational Moment: At the 48-hour mark, your body increases autophagy in hematopoietic stem cells, which are responsible for creating new blood and immune cells. You're rebuilding your immune system from the ground up! 🧫",
                    "Motivation: You've reached a level of fasting that puts you in an elite category! Less than 1% of people in modern society regularly experience this profound state of cellular rejuvenation. 🏆",
                    "Fun Fact: During prolonged fasting, your body conserves protein by becoming more efficient at recycling amino acids and reducing protein breakdown. You're preserving muscle while burning fat! 💪",
                    "Science Bite: At this stage, your body has significantly reduced IGF-1 (Insulin-like Growth Factor 1), which is associated with aging and cancer risk when chronically elevated. You're turning back your biological clock! ⏰",
                    "Humor Break: Your immune system right now: 'Finally, a chance to clean house! Out with the old guards, in with the fresh recruits. This place is going to be SPOTLESS!' 🧼"
                )
                randomContent(titles, shortTexts, longTexts)
            }
            FastingState.EXTENDED_FAST -> {
                val titles = listOf(
                    "Extended Fast Mastery! 🏆",
                    "Cellular Rejuvenation Maximized! ✨",
                    "72-Hour Fasting Champion! 👑",
                    "Maximum Autophagy Achieved! 🔝",
                    "Cellular Reset Complete! 🔄",
                    "Fasting Grand Master Level! 🥇",
                    "Anti-Aging Protocol: Activated! ⏳",
                    "72hr Biological Transformation! 🦋"
                )
                val shortTexts = listOf(
                    "72+ hours - deep cellular rejuvenation!",
                    "Extended Fast achieved at $formattedTime!",
                    "Maximum rejuvenation mode activated!",
                    "Insulin sensitivity improved by 70%!",
                    "BDNF levels soaring - brain growing!",
                    "72hr mark: Complete metabolic reset!",
                    "You've reached fasting enlightenment!",
                    "Cellular repair at maximum capacity!"
                )
                val longTexts = listOf(
                    "Fun Fact: Your insulin sensitivity has improved by up to 70% by now! Your cells are super-responsive to insulin, which helps prevent diabetes and metabolic syndrome. 📉",
                    "Science Bite: BDNF (Brain-Derived Neurotrophic Factor) levels increase significantly at this stage, promoting the growth of new brain cells and protecting existing ones. Your brain is literally growing! 🧠",
                    "Did you know? At this stage, your body has activated AMPK pathways that promote cellular repair and longevity. You're experiencing one of the most profound anti-aging interventions known to science! ⏳",
                    "Educational Moment: After 72 hours of fasting, your body has maximized autophagy and is now in a state of profound renewal. Studies show that this duration of fasting can trigger a near-complete immune system regeneration! 🧬",
                    "Motivation: You've achieved what few in modern society ever experience - a complete metabolic reset and cellular renovation. Your dedication to health is truly remarkable! 🌟",
                    "Fun Fact: Extended fasting triggers the expression of sirtuins, a group of proteins that regulate cellular health, stress resistance, and longevity. You're activating your body's built-in longevity program! 🧪",
                    "Science Bite: At this stage, your body has significantly upregulated FOX03 gene expression, which is associated with extreme longevity in humans. You're activating the same genetic pathways found in centenarians! 👵👴",
                    "Humor Break: Your cells right now: 'Is this a spa retreat? We've been cleaned, repaired, and rejuvenated! We feel like we've been completely remodeled!' 🏗️"
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
        
        // Since minSdk is 27 (Android 8.1), we can always use VibrationEffect (API 26)
        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
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