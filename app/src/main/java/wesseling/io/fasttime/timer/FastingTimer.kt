package wesseling.io.fasttime.timer

import android.content.Context
import android.content.SharedPreferences
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
    var maxFastingState by mutableStateOf(FastingState.NOT_FASTING)
        private set
    
    private var timerJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Start time in system millis
    private var startTimeMillis: Long = 0
    
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
                val states = FastingState.values()
                if (savedMaxState in states.indices) {
                    maxFastingState = states[savedMaxState]
                    Log.d(TAG, "Set max fasting state: ${maxFastingState.name}")
                } else {
                    Log.e(TAG, "Invalid max fasting state index: $savedMaxState")
                    maxFastingState = FastingState.NOT_FASTING
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting max fasting state", e)
                maxFastingState = FastingState.NOT_FASTING
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
        maxFastingState = FastingState.NOT_FASTING
        
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
        } catch (e: Exception) {
            Log.e(TAG, "Error starting timer", e)
            resetToSafeState()
        }
    }
    
    /**
     * Start the timer from a saved state
     */
    private fun startTimerFromSavedState() {
        try {
            // Cancel any existing job first
            timerJob?.cancel()
            
            timerJob = coroutineScope.launch {
                var lastUpdateTime = System.currentTimeMillis()
                
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
                        updateFastingState()
                        
                        lastUpdateTime = currentTime
                        delay(1000) // Update every second
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
     * Stop the timer
     */
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
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping timer", e)
            resetToSafeState()
        }
    }
    
    /**
     * Reset the timer to zero and stop it
     * @return CompletedFast object if the timer was running, null otherwise
     */
    fun resetTimer(): CompletedFast? {
        try {
            // Create a completed fast object if the timer was running
            val completedFast = if (isRunning || elapsedTimeMillis > 0) {
                CompletedFast(
                    startTimeMillis = startTimeMillis,
                    endTimeMillis = System.currentTimeMillis(),
                    durationMillis = elapsedTimeMillis,
                    maxFastingState = maxFastingState
                )
            } else {
                null
            }
            
            resetToSafeState()
            return completedFast
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting timer", e)
            resetToSafeState()
            return null
        }
    }
    
    /**
     * Toggle the timer state (start/stop)
     */
    fun toggleTimer() {
        if (isRunning) {
            stopTimer()
        } else {
            startTimer()
        }
    }
    
    /**
     * Update the fasting state based on elapsed time
     */
    private fun updateFastingState() {
        try {
            val elapsedHours = TimeUnit.MILLISECONDS.toHours(elapsedTimeMillis)
            if (elapsedHours < 0) {
                Log.e(TAG, "Negative elapsed hours detected, resetting timer")
                resetToSafeState()
                return
            }

            val previousState = currentFastingState
            currentFastingState = FastingState.getStateForHours(elapsedHours.toInt())
            
            // Update max fasting state if current state is higher
            if (currentFastingState.ordinal > maxFastingState.ordinal) {
                maxFastingState = currentFastingState
                saveState() // Save when max state changes
                
                // Check if we should send a notification for the new fasting state
                if (currentFastingState != previousState && currentFastingState != FastingState.NOT_FASTING) {
                    checkAndSendFastingStateNotification()
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
            editor.putInt(KEY_MAX_FASTING_STATE, maxFastingState.ordinal)
            
            Log.d(TAG, "Saving state: running=$isRunning, startTime=$startTimeMillis, elapsed=$elapsedTimeMillis")
            
            val success = editor.commit()
            if (!success) {
                Log.e(TAG, "Failed to save timer state")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving timer state", e)
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