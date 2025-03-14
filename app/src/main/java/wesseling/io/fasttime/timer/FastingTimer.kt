package wesseling.io.fasttime.timer

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.BatteryManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import wesseling.io.fasttime.model.CompletedFast
import wesseling.io.fasttime.model.FastingState
import wesseling.io.fasttime.notifications.NotificationHelper
import wesseling.io.fasttime.settings.PreferencesManager
import java.util.concurrent.TimeUnit
import wesseling.io.fasttime.util.DateTimeFormatter

/**
 * Manages the fasting timer functionality
 */
class FastingTimer private constructor(private val appContext: Context) : DefaultLifecycleObserver {
    // Timer state
    var isRunning by mutableStateOf(false)
        private set
    
    // Elapsed time in milliseconds
    var elapsedTimeMillis by mutableStateOf(0L)
        private set
    
    // Current fasting state
    var currentFastingState by mutableStateOf(FastingState.NOT_FASTING)
        private set
    
    // Maximum fasting state reached
    private var _maxFastingState by mutableStateOf(FastingState.NOT_FASTING)
    val maxFastingState: FastingState
        get() = _maxFastingState
    
    private var timerJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Start time in system millis
    private var startTimeMillis: Long = 0
    
    // Constants
    private val HOUR_IN_MILLIS = TimeUnit.HOURS.toMillis(1)
    
    // Shared preferences for persistence
    private val prefs: SharedPreferences = appContext.getSharedPreferences(
        PREFS_NAME, 
        Context.MODE_PRIVATE
    )
    
    init {
        initializeState()
    }

    private fun initializeState() {
        try {
            Log.d(TAG, "Initializing timer state")
            
            // Initialize from shared preferences
            val savedIsRunning = prefs.getBoolean(KEY_IS_RUNNING, false)
            val savedStartTime = prefs.getLong(KEY_START_TIME, 0)
            val savedElapsedTime = prefs.getLong(KEY_ELAPSED_TIME, 0)
            val savedMaxState = prefs.getInt(KEY_MAX_FASTING_STATE, 0)
            
            Log.d(TAG, "Loaded state: running=$savedIsRunning, startTime=$savedStartTime, elapsed=$savedElapsedTime")

            // Validate saved data
            if (savedStartTime > System.currentTimeMillis() || savedElapsedTime < 0) {
                Log.e(TAG, "Invalid saved state detected: startTime=$savedStartTime, elapsedTime=$savedElapsedTime")
                resetToSafeState()
                return
            }

            // Check for inconsistent state
            if (savedIsRunning && savedStartTime <= 0) {
                Log.e(TAG, "Inconsistent state: running but no start time")
                resetToSafeState()
                return
            }

            if (savedIsRunning) {
                isRunning = true
                startTimeMillis = savedStartTime
                
                // Calculate current elapsed time
                val calculatedElapsedTime = System.currentTimeMillis() - startTimeMillis
                
                // Validate calculated elapsed time
                if (calculatedElapsedTime < 0 || calculatedElapsedTime > TimeUnit.DAYS.toMillis(30)) {
                    Log.e(TAG, "Invalid elapsed time calculated: $calculatedElapsedTime")
                    resetToSafeState()
                    return
                }
                
                elapsedTimeMillis = calculatedElapsedTime
                Log.d(TAG, "Resuming timer with elapsed time: $elapsedTimeMillis ms")
                
                updateFastingState()
                startTimerFromSavedState()
            } else {
                isRunning = false
                elapsedTimeMillis = savedElapsedTime
                Log.d(TAG, "Timer not running, elapsed time: $elapsedTimeMillis ms")
                updateFastingState()
            }

            // Set max fasting state
            try {
                val states = FastingState.entries
                if (savedMaxState in states.indices) {
                    _maxFastingState = states[savedMaxState]
                    Log.d(TAG, "Set max fasting state: ${_maxFastingState.name}")
                } else {
                    Log.e(TAG, "Invalid max fasting state index: $savedMaxState")
                    _maxFastingState = FastingState.NOT_FASTING
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting max fasting state", e)
                _maxFastingState = FastingState.NOT_FASTING
            }
            
            // Save the validated state
            saveState()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing timer state", e)
            resetToSafeState()
        }
    }

    private fun resetToSafeState() {
        Log.d(TAG, "Resetting to safe state")
        
        // Cancel any existing timer job
        timerJob?.cancel()
        timerJob = null
        
        // Reset all state variables
        isRunning = false
        elapsedTimeMillis = 0
        startTimeMillis = 0
        currentFastingState = FastingState.NOT_FASTING
        _maxFastingState = FastingState.NOT_FASTING
        
        // Clear saved state
        try {
            val editor = prefs.edit()
            editor.clear()
            val success = editor.commit()
            if (!success) {
                Log.e(TAG, "Failed to clear preferences")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing preferences", e)
        }
    }
    
    override fun onPause(owner: LifecycleOwner) {
        try {
            saveState()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onPause", e)
        }
    }
    
    override fun onDestroy(owner: LifecycleOwner) {
        try {
            stopTimer()
            coroutineScope.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy", e)
        }
    }
    
    /**
     * Start or restart the timer
     */
    @Synchronized
    fun startTimer() {
        if (isRunning) return
        
        try {
            // Calculate new start time based on any existing elapsed time
            startTimeMillis = System.currentTimeMillis() - elapsedTimeMillis
            isRunning = true
            
            // Start the timer loop
            startTimerFromSavedState()
            
            // Save state to preferences
            saveState()
            
            // Update widgets
            updateWidgets()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting timer", e)
            resetToSafeState()
        }
    }
    
    /**
     * Start the timer from a saved state
     * 
     * This method is responsible for starting or resuming the timer based on saved state information.
     * It implements a sophisticated timer mechanism that:
     * 
     * 1. Handles background/foreground state detection to optimize update frequency
     * 2. Implements time inconsistency detection to prevent timer errors
     * 3. Provides adaptive update intervals based on elapsed time and app state
     * 4. Includes error handling with safe recovery mechanisms
     * 5. Implements battery-aware throttling to minimize energy consumption
     * 
     * The timer uses coroutines for efficient background processing and cancellation handling.
     * When the app is in the background, the update frequency is reduced to conserve battery.
     */
    private fun startTimerFromSavedState() {
        try {
            // Cancel any existing job first
            timerJob?.cancel()
            
            // Track state changes to minimize SharedPreferences writes
            var lastSavedElapsedTime = elapsedTimeMillis
            var lastSavedState = currentFastingState
            var lastSaveTime = System.currentTimeMillis()
            
            timerJob = coroutineScope.launch {
                var lastUpdateTime = System.currentTimeMillis()
                var inBackground = false
                var updateInterval: Long
                var consecutiveBackgroundUpdates = 0
                
                while (isRunning) {
                    try {
                        val currentTime = System.currentTimeMillis()
                        val expectedElapsed = currentTime - startTimeMillis
                        
                        // Check for time inconsistencies
                        if (currentTime < lastUpdateTime || expectedElapsed < 0) {
                            Log.e(TAG, "Time inconsistency detected")
                            resetToSafeState()
                            break
                        }
                        
                        elapsedTimeMillis = expectedElapsed
                        
                        // Check if app is in foreground using a more efficient method
                        val wasInBackground = inBackground
                        inBackground = isAppInBackground()
                        
                        // If transitioning from foreground to background, save state
                        if (!wasInBackground && inBackground) {
                            saveState()
                            lastSavedElapsedTime = elapsedTimeMillis
                            lastSavedState = currentFastingState
                            lastSaveTime = currentTime
                        }
                        
                        // Update fasting state
                        updateFastingState()
                        
                        // Determine if we need to save state based on significant changes
                        val timeSinceLastSave = currentTime - lastSaveTime
                        val elapsedTimeDifference = elapsedTimeMillis - lastSavedElapsedTime
                        val stateChanged = lastSavedState != currentFastingState
                        
                        // Save state if:
                        // 1. Fasting state has changed, or
                        // 2. It's been more than 5 minutes since last save, or
                        // 3. Elapsed time has changed by more than 5 minutes
                        if (stateChanged || 
                            timeSinceLastSave > TimeUnit.MINUTES.toMillis(5) || 
                            elapsedTimeDifference > TimeUnit.MINUTES.toMillis(5)) {
                            saveState()
                            lastSavedElapsedTime = elapsedTimeMillis
                            lastSavedState = currentFastingState
                            lastSaveTime = currentTime
                        }
                        
                        // Calculate adaptive update interval based on multiple factors
                        updateInterval = calculateAdaptiveUpdateInterval(inBackground, consecutiveBackgroundUpdates)
                        
                        // If in background, increment counter for progressive throttling
                        if (inBackground) {
                            consecutiveBackgroundUpdates++
                        } else {
                            consecutiveBackgroundUpdates = 0
                        }
                        
                        lastUpdateTime = currentTime
                        delay(updateInterval)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in timer loop", e)
                        // Only reset if it's not a cancellation
                        if (e !is kotlinx.coroutines.CancellationException) {
                            resetToSafeState()
                        }
                        break
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting timer from saved state", e)
            resetToSafeState()
        }
    }
    
    /**
     * Calculate an adaptive update interval based on multiple factors to optimize battery usage
     * 
     * @param inBackground Whether the app is in the background
     * @param consecutiveBackgroundUpdates Number of consecutive updates while in background
     * @return The calculated update interval in milliseconds
     */
    private fun calculateAdaptiveUpdateInterval(inBackground: Boolean, consecutiveBackgroundUpdates: Int): Long {
        // Base interval depends on whether app is in foreground or background
        val baseInterval = if (inBackground) {
            // When in background, start with 5 seconds
            5000L
        } else {
            // When in foreground, update every second
            1000L
        }
        
        // If in foreground, just return the base interval
        if (!inBackground) {
            return baseInterval
        }
        
        // For background updates, apply progressive throttling based on elapsed time
        val elapsedHours = TimeUnit.MILLISECONDS.toHours(elapsedTimeMillis).toInt()
        
        // Calculate time-based multiplier
        val timeBasedMultiplier = when {
            // Near state transitions (within 10 minutes), update more frequently
            isNearStateTransition(elapsedHours) -> 1.0
            
            // First hour of fasting, update every 5 seconds in background
            elapsedHours < 1 -> 1.0
            
            // 1-4 hours, gradually increase interval
            elapsedHours < 4 -> 2.0
            
            // 4-12 hours, further increase interval
            elapsedHours < 12 -> 3.0
            
            // 12-24 hours
            elapsedHours < 24 -> 4.0
            
            // After 24 hours, much less frequent updates needed
            else -> 6.0
        }
        
        // Apply progressive throttling based on consecutive background updates
        // This gradually reduces update frequency the longer the app stays in background
        val consecutiveUpdateMultiplier = when {
            consecutiveBackgroundUpdates < 10 -> 1.0
            consecutiveBackgroundUpdates < 30 -> 1.5
            consecutiveBackgroundUpdates < 60 -> 2.0
            consecutiveBackgroundUpdates < 120 -> 3.0
            else -> 4.0
        }
        
        // Apply battery-aware throttling
        val batteryMultiplier = getBatteryAwareThrottlingMultiplier()
        
        // Calculate final interval with all multipliers
        val finalInterval = (baseInterval * timeBasedMultiplier * consecutiveUpdateMultiplier * batteryMultiplier).toLong()
        
        // Cap the maximum interval to 10 minutes to ensure reasonable responsiveness
        return finalInterval.coerceAtMost(TimeUnit.MINUTES.toMillis(10))
    }
    
    /**
     * Get a throttling multiplier based on battery level and charging state
     * 
     * @return A multiplier to apply to the update interval
     */
    private fun getBatteryAwareThrottlingMultiplier(): Double {
        try {
            val batteryInfo = getBatteryInfo()
            val batteryLevel = batteryInfo.first
            val isCharging = batteryInfo.second
            
            // If charging, no need to throttle
            if (isCharging) {
                return 1.0
            }
            
            // Apply throttling based on battery level
            return when {
                batteryLevel <= 15 -> 2.0  // Critical battery level
                batteryLevel <= 30 -> 1.5  // Low battery level
                else -> 1.0               // Normal battery level
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting battery info", e)
            return 1.0
        }
    }
    
    /**
     * Get battery level and charging state
     * 
     * @return Pair of (batteryLevel, isCharging)
     */
    private fun getBatteryInfo(): Pair<Int, Boolean> {
        val batteryIntent = appContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryLevel = if (level != -1 && scale != -1) (level * 100 / scale) else 50
        
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || 
                         status == BatteryManager.BATTERY_STATUS_FULL
        
        return Pair(batteryLevel, isCharging)
    }
    
    /**
     * Check if the app is in background using a more efficient method
     * 
     * @return true if the app is in background, false otherwise
     */
    private fun isAppInBackground(): Boolean {
        try {
            val activityManager = appContext.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            val appProcesses = activityManager.runningAppProcesses ?: return true
            
            val packageName = appContext.packageName
            for (appProcess in appProcesses) {
                if (appProcess.processName == packageName) {
                    return appProcess.importance != android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                }
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if app is in background", e)
            return true // Assume background to be safe
        }
    }
    
    /**
     * Check if the current elapsed time is near a state transition
     */
    private fun isNearStateTransition(elapsedHours: Int): Boolean {
        // State transitions occur at 4, 12, 18, 24, 48, and 72 hours
        val stateTransitions = listOf(4, 12, 18, 24, 48, 72)
        
        // Check if we're within 10 minutes of any transition
        for (transition in stateTransitions) {
            val minutesToTransition = Math.abs((elapsedHours * 60) - (transition * 60))
            if (minutesToTransition <= 10) {
                return true
            }
        }
        
        return false
    }
    
    /**
     * Stop the timer
     */
    @Synchronized
    fun stopTimer() {
        try {
            isRunning = false
            timerJob?.cancel()
            timerJob = null
            
            // Ensure elapsed time is accurate before saving
            if (startTimeMillis > 0) {
                elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis
            }
            
            // Save state to preferences
            saveState()
            
            // Update widgets
            updateWidgets()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping timer", e)
            resetToSafeState()
        }
    }
    
    /**
     * Reset the timer to zero and stop it
     * 
     * This method performs a complete reset of the fasting timer and returns a CompletedFast object
     * if the timer was running or had elapsed time. The method:
     * 
     * 1. Creates a CompletedFast object with the current timer state (if applicable)
     * 2. Resets all timer state variables to their initial values
     * 3. Updates widgets to reflect the timer reset
     * 4. Implements comprehensive error handling
     * 
     * The CompletedFast object contains all relevant information about the completed fasting session,
     * including start time, end time, duration, and maximum fasting state achieved.
     * 
     * @return CompletedFast object if the timer was running or had elapsed time, null otherwise
     */
    @Synchronized
    fun resetTimer(): CompletedFast? {
        try {
            // Create a completed fast object if the timer was running
            val completedFast = if (isRunning || elapsedTimeMillis > 0) {
                CompletedFast(
                    startTimeMillis = startTimeMillis,
                    endTimeMillis = System.currentTimeMillis(),
                    durationMillis = elapsedTimeMillis,
                    maxFastingState = _maxFastingState
                )
            } else {
                null
            }
            
            resetToSafeState()
            
            // Update widgets
            updateWidgets()
            
            // Log the completed fast details
            if (completedFast != null) {
                Log.d(TAG, "Created completed fast: id=${completedFast.id}, duration=${completedFast.durationMillis}, state=${completedFast.maxFastingState}")
            }
            
            return completedFast
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting timer", e)
            resetToSafeState()
            return null
        }
    }
    
    /**
     * Update the current fasting state based on elapsed time
     * 
     * This method is responsible for determining the current fasting state based on the elapsed time
     * and managing state transitions. It performs several key functions:
     * 
     * 1. Calculates the appropriate fasting state based on elapsed time thresholds
     * 2. Detects state changes and triggers notifications when appropriate
     * 3. Updates the maximum fasting state achieved during the current fast
     * 4. Triggers widget updates when the state changes
     * 5. Implements error handling to prevent crashes
     * 
     * The fasting states follow a progression based on scientific research about the physiological
     * changes that occur during fasting. Each state represents a different set of metabolic processes
     * and health benefits.
     */
    private fun updateFastingState() {
        try {
            val previousState = currentFastingState
            
            // Determine the current fasting state based on elapsed time
            currentFastingState = when {
                !isRunning -> FastingState.NOT_FASTING
                elapsedTimeMillis < 4 * HOUR_IN_MILLIS -> FastingState.NOT_FASTING
                elapsedTimeMillis < 12 * HOUR_IN_MILLIS -> FastingState.EARLY_FAST
                elapsedTimeMillis < 18 * HOUR_IN_MILLIS -> FastingState.GLYCOGEN_DEPLETION
                elapsedTimeMillis < 24 * HOUR_IN_MILLIS -> FastingState.METABOLIC_SHIFT
                elapsedTimeMillis < 48 * HOUR_IN_MILLIS -> FastingState.DEEP_KETOSIS
                elapsedTimeMillis < 72 * HOUR_IN_MILLIS -> FastingState.IMMUNE_RESET
                else -> FastingState.EXTENDED_FAST
            }
            
            // Check if the state has changed
            val stateChanged = previousState != currentFastingState
            
            // Update max fasting state if current state is higher
            if (currentFastingState.ordinal > _maxFastingState.ordinal) {
                _maxFastingState = currentFastingState
                saveState() // Save when max state changes
            }
            
            // Send notification when state changes to a new state (not NOT_FASTING)
            if (stateChanged && currentFastingState != FastingState.NOT_FASTING) {
                checkAndSendFastingStateNotification()
            }
            
            // Only update widgets when necessary to save battery
            if (stateChanged) {
                // Always update widgets when state changes
                Log.d(TAG, "Fasting state changed from ${previousState.name} to ${currentFastingState.name}, updating widgets")
                updateWidgets(true) // Force update when state changes
            } else if (isRunning) {
                // For running timer, update widgets less frequently based on elapsed time
                val updateIntervalMinutes = when {
                    elapsedTimeMillis < HOUR_IN_MILLIS -> 1 // Update every minute in the first hour
                    elapsedTimeMillis < 4 * HOUR_IN_MILLIS -> 2 // Every 2 minutes for 1-4 hours
                    elapsedTimeMillis < 12 * HOUR_IN_MILLIS -> 5 // Every 5 minutes for 4-12 hours
                    else -> 10 // Every 10 minutes after 12 hours
                }
                
                // Only update if enough time has passed since the last update
                val currentTimeMinutes = System.currentTimeMillis() / (60 * 1000)
                if (currentTimeMinutes % updateIntervalMinutes == 0L) {
                    updateWidgets(false)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating fasting state", e)
        }
    }
    
    /**
     * Check if notifications are enabled and send a notification for the current fasting state
     */
    private fun checkAndSendFastingStateNotification() {
        try {
            // Get preferences manager to check if notifications are enabled
            val preferencesManager = PreferencesManager.getInstance(appContext)
            val notificationsEnabled = preferencesManager.dateTimePreferences.enableFastingStateNotifications
            
            if (notificationsEnabled) {
                // Create and send notification
                val notificationHelper = NotificationHelper(appContext)
                notificationHelper.sendFastingStateNotification(currentFastingState)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending notification", e)
        }
    }
    
    /**
     * Adjust the start time of the fast
     * @param adjustmentMillis The amount to adjust in milliseconds (positive to start earlier, negative to start later)
     * @return Boolean indicating if the adjustment was successful
     */
    fun adjustStartTime(adjustmentMillis: Long): Boolean {
        if (!isRunning && elapsedTimeMillis == 0L) return false // Can't adjust if not running and no elapsed time
        
        // Calculate new start time
        val newStartTimeMillis = startTimeMillis - adjustmentMillis
        
        // Prevent adjustments that would result in start time being after current time
        if (newStartTimeMillis > System.currentTimeMillis()) {
            return false
        }
        
        // Apply the adjustment
        startTimeMillis = newStartTimeMillis
        
        // Update elapsed time
        elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis
        
        // Update fasting state
        updateFastingState()
        
        // Save state to preferences
        saveState()
        
        return true
    }
    
    /**
     * Format elapsed time as HH:MM:SS
     */
    fun getFormattedTime(): String {
        return DateTimeFormatter.formatElapsedTime(elapsedTimeMillis)
    }
    
    /**
     * Save timer state to shared preferences
     */
    private fun saveState() {
        try {
            // Validate state before saving
            if (startTimeMillis > System.currentTimeMillis() && isRunning) {
                Log.e(TAG, "Invalid state detected before saving: startTime=$startTimeMillis is in the future")
                return
            }
            
            if (elapsedTimeMillis < 0) {
                Log.e(TAG, "Invalid elapsed time before saving: $elapsedTimeMillis")
                return
            }
            
            val editor = prefs.edit()
            editor.putBoolean(KEY_IS_RUNNING, isRunning)
            editor.putLong(KEY_START_TIME, startTimeMillis)
            editor.putLong(KEY_ELAPSED_TIME, elapsedTimeMillis)
            editor.putInt(KEY_MAX_FASTING_STATE, _maxFastingState.ordinal)
            
            Log.d(TAG, "Saving state: running=$isRunning, startTime=$startTimeMillis, elapsed=$elapsedTimeMillis")
            
            val success = editor.commit()
            if (!success) {
                Log.e(TAG, "Failed to save timer state")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving timer state", e)
        }
    }
    
    /**
     * Update all widgets with the current timer state
     */
    private fun updateWidgets(forceUpdate: Boolean = false) {
        try {
            // Use a more reliable approach to update widgets
            Log.d(TAG, "Updating widgets with current state: running=$isRunning, state=${_maxFastingState.name}, forceUpdate=$forceUpdate")
            
            // Send broadcast with current state information
            val widgetIntent = Intent("wesseling.io.fasttime.widget.ACTION_UPDATE_WIDGETS").apply {
                setPackage(appContext.packageName)
                // Add state information to help debugging
                putExtra("IS_RUNNING", isRunning)
                putExtra("CURRENT_STATE", _maxFastingState.ordinal)
                putExtra("ELAPSED_TIME", elapsedTimeMillis)
                putExtra("TIMESTAMP", System.currentTimeMillis())
                putExtra("FORCE_UPDATE", forceUpdate)
                // Add flags to ensure delivery
                flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
            }
            appContext.sendBroadcast(widgetIntent)
            
            // Only use the direct update and delayed update for forced updates
            // to reduce unnecessary processing
            if (forceUpdate) {
                // Also directly update widgets as a fallback
                try {
                    val widgetProviderClass = Class.forName("wesseling.io.fasttime.widget.FastingWidgetProvider")
                    val updateMethod = widgetProviderClass.getDeclaredMethod("updateAllWidgets", Context::class.java, Boolean::class.java)
                    updateMethod.invoke(null, appContext, forceUpdate)
                    Log.d(TAG, "Direct widget update succeeded")
                } catch (e: Exception) {
                    // This is just a fallback, so log but don't throw
                    Log.d(TAG, "Direct widget update failed: ${e.message}")
                }
                
                // Schedule a delayed update to ensure the widget is updated
                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        Log.d(TAG, "Performing delayed widget update")
                        val delayedIntent = Intent("wesseling.io.fasttime.widget.ACTION_UPDATE_WIDGETS").apply {
                            setPackage(appContext.packageName)
                            putExtra("DELAYED_UPDATE", true)
                            putExtra("FORCE_UPDATE", forceUpdate)
                        }
                        appContext.sendBroadcast(delayedIntent)
                    } catch (e: Exception) {
                        Log.d(TAG, "Delayed widget update failed: ${e.message}")
                    }
                }, 500) // 500ms delay
            }
        } catch (e: Exception) {
            // Widget provider might not be available, ignore
            Log.d(TAG, "Could not update widgets: ${e.message}")
        }
    }
    
    companion object {
        private const val TAG = "FastingTimer"
        private const val PREFS_NAME = "wesseling.io.fasttime.fasting_timer"
        private const val KEY_IS_RUNNING = "is_running"
        private const val KEY_START_TIME = "start_time"
        private const val KEY_ELAPSED_TIME = "elapsed_time"
        private const val KEY_MAX_FASTING_STATE = "max_fasting_state"
        
        @Volatile
        private var instance: FastingTimer? = null
        
        fun getInstance(context: Context): FastingTimer {
            return instance ?: synchronized(this) {
                Log.d(TAG, "Creating new FastingTimer instance")
                instance ?: FastingTimer(context.applicationContext).also { newInstance ->
                    instance = newInstance
                    // If context is a LifecycleOwner, register the observer
                    if (context is LifecycleOwner) {
                        Log.d(TAG, "Registering lifecycle observer")
                        context.lifecycle.addObserver(newInstance)
                    }
                }
            }
        }

        fun destroyInstance() {
            synchronized(this) {
                Log.d(TAG, "Destroying FastingTimer instance")
                val currentInstance = instance
                if (currentInstance != null) {
                    try {
                        // Save state before destroying
                        currentInstance.saveState()
                        
                        // Stop timer and cancel coroutines
                        currentInstance.isRunning = false
                        currentInstance.timerJob?.cancel()
                        currentInstance.timerJob = null
                        currentInstance.coroutineScope.cancel()
                        
                        // Remove observer if context was a LifecycleOwner
                        val appContext = currentInstance.appContext
                        if (appContext is LifecycleOwner) {
                            Log.d(TAG, "Removing lifecycle observer")
                            appContext.lifecycle.removeObserver(currentInstance)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error during instance destruction", e)
                    }
                }
                instance = null
                Log.d(TAG, "FastingTimer instance set to null")
            }
        }
    }
} 