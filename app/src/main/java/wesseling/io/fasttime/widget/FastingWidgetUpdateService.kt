package wesseling.io.fasttime.widget

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
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
 * Includes fallback mechanisms to ensure service reliability
 */
class FastingWidgetUpdateService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            try {
                Log.d(TAG, "Running widget update")
                
                // Update all widgets
                FastingWidgetProvider.updateAllWidgets(this@FastingWidgetUpdateService)
                
                // Schedule next update with adaptive interval
                scheduleNextUpdateWithAdaptiveInterval()
                
                // Set next backup alarm in case service is killed
                scheduleBackupAlarm()
                
                // Update service health timestamp
                updateServiceHealthTimestamp()
                
                Log.d(TAG, "Widget update completed")
            } catch (e: Exception) {
                Log.e(TAG, "Error in update runnable", e)
                // Try to recover by scheduling next update anyway
                handler.postDelayed(this, FALLBACK_UPDATE_INTERVAL)
                
                // Ensure backup alarm is set in case of continued failures
                scheduleBackupAlarm()
            }
        }
    }
    
    // Broadcast receiver for alarm-based backup updates
    private val alarmReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "Received backup alarm - attempting service recovery")
            when (intent.action) {
                ACTION_BACKUP_ALARM -> {
                    // Check if service is healthy
                    if (!isServiceHealthy()) {
                        Log.w(TAG, "Service recovery triggered by backup alarm")
                        // Start update now
                        handler.removeCallbacks(updateRunnable)
                        handler.post(updateRunnable)
                    } else {
                        Log.d(TAG, "Service is healthy, backup alarm not needed")
                    }
                    
                    // Schedule next backup alarm regardless
                    scheduleBackupAlarm()
                }
                ACTION_HEALTH_CHECK -> {
                    // Perform service health check
                    performHealthCheck()
                }
            }
        }
    }
    
    companion object {
        private const val TAG = "WidgetUpdateService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "fasting_widget_channel"
        
        // Service recovery constants
        private const val ACTION_BACKUP_ALARM = "wesseling.io.fasttime.widget.ACTION_BACKUP_ALARM"
        private const val ACTION_HEALTH_CHECK = "wesseling.io.fasttime.widget.ACTION_HEALTH_CHECK"
        private const val BACKUP_ALARM_REQUEST_CODE = 1002
        private const val HEALTH_CHECK_REQUEST_CODE = 1003
        private const val PREFS_NAME = "widget_service_prefs"
        private const val KEY_LAST_UPDATE_TIMESTAMP = "last_update_timestamp"
        private const val KEY_HEALTH_CHECK_COUNT = "health_check_count"
        private const val MAX_HEALTH_CHECK_FAILURES = 3
        private const val FALLBACK_UPDATE_INTERVAL = 5 * 60 * 1000L // 5 minutes
        private const val HEALTH_CHECK_INTERVAL = 15 * 60 * 1000L // 15 minutes
        
        // Battery thresholds
        private const val BATTERY_LOW_THRESHOLD = 15 // 15%
        private const val BATTERY_MEDIUM_THRESHOLD = 30 // 30%
        
        /**
         * Check if the service is running by checking a timestamp
         * This can be called from outside the service to verify its health
         */
        fun isServiceRunning(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val lastTimestamp = prefs.getLong(KEY_LAST_UPDATE_TIMESTAMP, 0)
            val currentTime = System.currentTimeMillis()
            
            // If last update was more than 15 minutes ago, consider service not running
            val threshold = TimeUnit.MINUTES.toMillis(15)
            return (currentTime - lastTimestamp) < threshold
        }
        
        /**
         * Ensure the service is running by starting it if needed
         * This provides a safe way for other components to restart the service
         */
        fun ensureServiceRunning(context: Context) {
            if (!isServiceRunning(context)) {
                Log.d(TAG, "Service not running, attempting to start it")
                try {
                    val intent = Intent(context, FastingWidgetUpdateService::class.java)
                    intent.putExtra("recovery", true)
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to restart service", e)
                    // Set a backup alarm as last resort
                    setImmediateBackupAlarm(context)
                }
            }
        }
        
        /**
         * Set an immediate backup alarm to recover service
         * This is used as a last resort when direct service start fails
         */
        private fun setImmediateBackupAlarm(context: Context) {
            try {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(context, FastingWidgetUpdateService::class.java).apply {
                    action = ACTION_BACKUP_ALARM
                }
                
                val pendingIntent = PendingIntent.getBroadcast(
                    context, 
                    BACKUP_ALARM_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                // Set alarm to trigger in 30 seconds
                val triggerTime = SystemClock.elapsedRealtime() + 30 * 1000
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
                
                Log.d(TAG, "Immediate backup alarm set")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set immediate backup alarm", e)
            }
        }
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
        
        // Register backup alarm receiver
        val filter = IntentFilter().apply {
            addAction(ACTION_BACKUP_ALARM)
            addAction(ACTION_HEALTH_CHECK)
        }
        registerReceiver(alarmReceiver, filter)
        
        // Schedule health check
        scheduleHealthCheck()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started, flags=$flags, startId=$startId, recovery=${intent?.getBooleanExtra("recovery", false)}")
        
        try {
            // Check if timer is actually running; if not, stop the service to save battery
            val fastingTimer = FastingTimer.getInstance(this)
            if (!fastingTimer.isRunning) {
                Log.d(TAG, "Timer not running, stopping service to save battery")
                stopSelf()
                return START_NOT_STICKY
            }
            
            // Create a notification for the foreground service
            val notification = createNotification()
            
            // Start as a foreground service with higher priority
            startForeground(NOTIFICATION_ID, notification)
            
            // Reset health check count on successful start
            resetHealthCheckCount()
            
            // Update service health timestamp
            updateServiceHealthTimestamp()
            
            // Start the update loop with immediate first update
            handler.removeCallbacks(updateRunnable) // Remove any existing callbacks
            handler.post(updateRunnable)
            
            // Schedule backup alarm for recovery
            scheduleBackupAlarm()
            
            // Schedule an immediate widget update
            FastingWidgetProvider.updateAllWidgets(this)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting service", e)
            
            // Schedule a fallback update even on failure
            handler.postDelayed(updateRunnable, FALLBACK_UPDATE_INTERVAL)
            
            // Set backup alarm as failsafe
            scheduleBackupAlarm()
        }
        
        // Use START_REDELIVER_INTENT to have the system redeliver the intent if service is killed
        return START_REDELIVER_INTENT
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        
        try {
            // Unregister receiver
            unregisterReceiver(alarmReceiver)
            
            // Remove callbacks
            handler.removeCallbacks(updateRunnable)
            
            // Set backup alarm to recover if the service was killed unexpectedly
            // This helps in cases where the system kills the service due to resource constraints
            val fastingTimer = FastingTimer.getInstance(this)
            if (fastingTimer.isRunning) {
                Log.d(TAG, "Timer still running, setting backup alarm before service destruction")
                scheduleBackupAlarm()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during service destruction", e)
        }
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
            
            // If timer is not running, stop the service instead of scheduling updates
            if (!fastingTimer.isRunning) {
                Log.d(TAG, "Timer not running, stopping widget update service to save battery")
                stopSelf()
                return
            }
            
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
                
                // This case should never be reached now that we're stopping the service
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
            handler.postDelayed(updateRunnable, FALLBACK_UPDATE_INTERVAL)
            
            // Ensure backup alarm is set
            scheduleBackupAlarm()
        }
    }
    
    /**
     * Schedule a backup alarm to ensure service continuity if killed
     * This alarm acts as a failsafe mechanism to restart updates if the service dies
     */
    private fun scheduleBackupAlarm() {
        try {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, FastingWidgetUpdateService::class.java).apply {
                action = ACTION_BACKUP_ALARM
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                this, 
                BACKUP_ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Set alarm for 10 minutes from now
            // This is our safety net if the service gets killed
            val triggerTime = SystemClock.elapsedRealtime() + TimeUnit.MINUTES.toMillis(10)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            
            Log.d(TAG, "Backup alarm scheduled")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule backup alarm", e)
        }
    }
    
    /**
     * Schedule a periodic health check to verify service is running correctly
     */
    private fun scheduleHealthCheck() {
        try {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, FastingWidgetUpdateService::class.java).apply {
                action = ACTION_HEALTH_CHECK
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                this, 
                HEALTH_CHECK_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Schedule health check every 15 minutes
            val triggerTime = SystemClock.elapsedRealtime() + HEALTH_CHECK_INTERVAL
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            
            Log.d(TAG, "Health check scheduled")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule health check", e)
        }
    }
    
    /**
     * Perform a health check on the service
     * If health checks repeatedly fail, it indicates the service is not updating properly
     */
    private fun performHealthCheck() {
        try {
            Log.d(TAG, "Performing service health check")
            
            if (!isServiceHealthy()) {
                val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                var failureCount = prefs.getInt(KEY_HEALTH_CHECK_COUNT, 0) + 1
                
                prefs.edit().putInt(KEY_HEALTH_CHECK_COUNT, failureCount).apply()
                Log.w(TAG, "Service health check failed ($failureCount/$MAX_HEALTH_CHECK_FAILURES)")
                
                if (failureCount >= MAX_HEALTH_CHECK_FAILURES) {
                    Log.e(TAG, "Service health check failed repeatedly, attempting recovery")
                    // Reset failure count
                    resetHealthCheckCount()
                    
                    // Force update now
                    handler.removeCallbacks(updateRunnable)
                    handler.post(updateRunnable)
                }
            } else {
                // Reset failure count on successful health check
                resetHealthCheckCount()
                Log.d(TAG, "Service health check passed")
            }
            
            // Schedule next health check
            scheduleHealthCheck()
        } catch (e: Exception) {
            Log.e(TAG, "Error during health check", e)
            
            // Schedule next health check anyway
            scheduleHealthCheck()
        }
    }
    
    /**
     * Reset the health check failure count
     */
    private fun resetHealthCheckCount() {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_HEALTH_CHECK_COUNT, 0)
            .apply()
    }
    
    /**
     * Update the timestamp indicating the service is healthy
     */
    private fun updateServiceHealthTimestamp() {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_LAST_UPDATE_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }
    
    /**
     * Check if the service is currently healthy based on the last update timestamp
     */
    private fun isServiceHealthy(): Boolean {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastTimestamp = prefs.getLong(KEY_LAST_UPDATE_TIMESTAMP, 0)
        val currentTime = System.currentTimeMillis()
        
        // If last update was more than 15 minutes ago, service is not healthy
        val maxGap = TimeUnit.MINUTES.toMillis(15)
        return (currentTime - lastTimestamp) < maxGap
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
     * When the device is in power save mode, the app adjusts its behavior to further
     * reduce battery consumption by extending update intervals.
     * 
     * @return true if the device is in power save mode, false otherwise
     */
    private fun isPowerSaveMode(): Boolean {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isPowerSaveMode
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