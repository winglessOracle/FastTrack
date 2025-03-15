package wesseling.io.fasttime.widget

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import wesseling.io.fasttime.MainActivity
import wesseling.io.fasttime.R
import wesseling.io.fasttime.settings.PreferencesManager
import wesseling.io.fasttime.timer.FastingTimer
import java.util.concurrent.TimeUnit

/**
 * Service to update the fasting widget periodically
 * Implements battery-aware update intervals to conserve battery
 */
class FastingWidgetUpdateService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            try {
                Log.d(TAG, "Running widget update")
                
                // Update all widgets
                FastingWidgetProvider.updateAllWidgets(this@FastingWidgetUpdateService)
                
                // Also update large widgets
                FastingWidgetLargeProvider.updateAllWidgets(this@FastingWidgetUpdateService)
                
                // Schedule next update with adaptive interval
                scheduleNextUpdateWithAdaptiveInterval()
                
                Log.d(TAG, "Widget update completed")
            } catch (e: Exception) {
                Log.e(TAG, "Error in update runnable", e)
                // Try to recover by scheduling next update anyway
                handler.postDelayed(this, TimeUnit.MINUTES.toMillis(5))
            }
        }
    }
    
    companion object {
        private const val TAG = "WidgetUpdateService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "fasting_widget_channel"
        
        // Battery thresholds
        private const val BATTERY_LOW_THRESHOLD = 15 // 15%
        private const val BATTERY_MEDIUM_THRESHOLD = 30 // 30%
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        
        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Fasting Widget Updates",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Used to keep the fasting widget updated"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        
        try {
            // Create a notification for the foreground service
            val notification = createNotification()
            
            // Start as a foreground service with higher priority
            startForeground(NOTIFICATION_ID, notification)
            
            // Start the update loop with immediate first update
            handler.removeCallbacks(updateRunnable) // Remove any existing callbacks
            handler.post(updateRunnable)
            
            // Schedule an immediate widget update
            FastingWidgetProvider.updateAllWidgets(this)
            
            // Also update large widgets
            FastingWidgetLargeProvider.updateAllWidgets(this)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting service", e)
        }
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        
        // Remove callbacks
        handler.removeCallbacks(updateRunnable)
    }
    
    /**
     * Create a notification for the foreground service
     */
    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("FastTrack Widget")
        .setContentText("Keeping your widget updated")
        .setSmallIcon(R.drawable.ic_play_arrow)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
        .setCategory(NotificationCompat.CATEGORY_SERVICE)
        .setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
        .build()
    
    /**
     * Schedule the next update with an adaptive interval based on fasting state, battery level, and user preferences
     * 
     * This method implements a sophisticated adaptive update strategy that balances update frequency
     * with battery consumption. It considers multiple factors:
     * 
     * 1. Current fasting state and progress - updates more frequently during critical periods
     * 2. Battery level and charging status - conserves battery when needed
     * 3. Power save mode - respects system battery saving
     * 4. User preferences - allows user control over update frequency
     * 
     * The algorithm first determines a base interval based on the fasting state and progress,
     * then applies adjustments based on battery conditions and user preferences.
     */
    private fun scheduleNextUpdateWithAdaptiveInterval() {
        try {
            val fastingTimer = FastingTimer.getInstance(this)
            
            // Get battery information
            val batteryInfo = getBatteryInfo()
            val batteryLevel = batteryInfo.first
            val isCharging = batteryInfo.second
            val isPowerSaveMode = isPowerSaveMode()
            
            // Get user's update frequency preference
            val preferencesManager = PreferencesManager.getInstance(this)
            val updateFrequencyMultiplier = preferencesManager.dateTimePreferences.updateFrequency.multiplier
            
            Log.d(TAG, "Battery level: $batteryLevel%, Charging: $isCharging, Power save: $isPowerSaveMode, " +
                       "Update frequency multiplier: $updateFrequencyMultiplier")
            
            // Base update interval based on fasting state
            val baseInterval = when {
                // When timer is running, update more frequently
                fastingTimer.isRunning -> {
                    val elapsedHours = TimeUnit.MILLISECONDS.toHours(fastingTimer.elapsedTimeMillis).toInt()
                    
                    // Update more frequently near state transitions
                    when {
                        // Near state transitions (within 10 minutes), update more frequently
                        isNearStateTransition(elapsedHours) -> TimeUnit.SECONDS.toMillis(60)
                        
                        // First hour of fasting, update every 2 minutes
                        elapsedHours < 1 -> TimeUnit.MINUTES.toMillis(2)
                        
                        // After 24 hours, update less frequently
                        elapsedHours >= 24 -> TimeUnit.MINUTES.toMillis(10)
                        
                        // Default update interval when running
                        else -> TimeUnit.MINUTES.toMillis(5)
                    }
                }
                
                // When not running, update very infrequently to save battery
                else -> TimeUnit.MINUTES.toMillis(30)
            }
            
            // Apply user's update frequency preference to the base interval
            val userAdjustedInterval = (baseInterval * updateFrequencyMultiplier).toLong()
            
            // Further adjust interval based on battery level and charging state
            val adjustedInterval = when {
                // If charging, we can use the user-adjusted interval
                isCharging -> userAdjustedInterval
                
                // If in power save mode, extend intervals significantly
                isPowerSaveMode -> userAdjustedInterval * 2
                
                // If battery is low, extend intervals
                batteryLevel <= BATTERY_LOW_THRESHOLD -> userAdjustedInterval * 1.5
                
                // If battery is medium, slightly extend intervals
                batteryLevel <= BATTERY_MEDIUM_THRESHOLD -> userAdjustedInterval * 1.2
                
                // Otherwise use the user-adjusted interval
                else -> userAdjustedInterval
            }.toLong()
            
            Log.d(TAG, "Next update scheduled in ${adjustedInterval/1000} seconds " +
                       "(base: ${baseInterval/1000}s, user-adjusted: ${userAdjustedInterval/1000}s)")
            
            // Ensure we don't have multiple callbacks
            handler.removeCallbacks(updateRunnable)
            handler.postDelayed(updateRunnable, adjustedInterval)
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling next update", e)
            // Fallback to a safe interval
            handler.postDelayed(updateRunnable, TimeUnit.MINUTES.toMillis(10))
        }
    }
    
    /**
     * Get battery level and charging state
     * 
     * This method retrieves the current battery status of the device by:
     * 1. Registering a receiver for the ACTION_BATTERY_CHANGED broadcast
     * 2. Extracting the battery level as a percentage (0-100)
     * 3. Determining if the device is currently charging or fully charged
     * 
     * The method includes fallback values (50% battery level) in case the
     * battery information cannot be retrieved, ensuring the app continues
     * to function even when battery data is unavailable.
     * 
     * @return Pair of (batteryLevel, isCharging) where batteryLevel is 0-100 and
     *         isCharging is true if the device is plugged in and charging or fully charged
     */
    private fun getBatteryInfo(): Pair<Int, Boolean> {
        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryLevel = if (level != -1 && scale != -1) (level * 100 / scale) else 50
        
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || 
                         status == BatteryManager.BATTERY_STATUS_FULL
        
        return Pair(batteryLevel, isCharging)
    }
    
    /**
     * Check if device is in power save mode
     * 
     * This method determines if the device is currently in power save mode (battery saver)
     * by querying the PowerManager system service. Power save mode is an Android feature
     * that restricts background activities to conserve battery.
     * 
     * The implementation is version-aware:
     * - For Android Lollipop (API 21) and above, it uses PowerManager.isPowerSaveMode
     * - For older versions, it defaults to false as the feature wasn't available
     * 
     * When the device is in power save mode, the app adjusts its behavior to further
     * reduce battery consumption by extending update intervals.
     * 
     * @return true if the device is in power save mode, false otherwise
     */
    private fun isPowerSaveMode(): Boolean {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            powerManager.isPowerSaveMode
        } else {
            false
        }
    }
    
    /**
     * Check if the current elapsed time is near a fasting state transition
     * 
     * This method determines if the current fasting duration is approaching a state transition point.
     * State transitions represent significant physiological changes during fasting and are important
     * moments to update the UI more frequently to provide timely feedback to the user.
     * 
     * The method:
     * 1. Defines key transition points at 0, 12, 18, and 24 hours, which correspond to:
     *    - 0h: Start of fasting
     *    - 12h: Transition to glycogen depletion
     *    - 18h: Transition to metabolic shift
     *    - 24h: Transition to deep ketosis
     * 
     * 2. Checks if the current elapsed time is within 10 minutes of any transition point
     * 
     * When near a transition, the widget update frequency is increased to ensure the user
     * receives timely notifications about their fasting progress and achievements.
     * 
     * @param elapsedHours The current duration of the fast in hours
     * @return true if within 10 minutes of a state transition, false otherwise
     */
    private fun isNearStateTransition(elapsedHours: Int): Boolean {
        // State transitions occur at 0, 12, 18, and 24 hours
        val stateTransitions = listOf(0, 12, 18, 24)
        
        // Check if we're within 10 minutes of any transition
        for (transition in stateTransitions) {
            val minutesToTransition = Math.abs((elapsedHours * 60) - (transition * 60))
            if (minutesToTransition <= 10) {
                return true
            }
        }
        
        return false
    }
} 