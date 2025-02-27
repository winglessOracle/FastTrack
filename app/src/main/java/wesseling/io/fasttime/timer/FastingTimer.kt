package wesseling.io.fasttime.timer

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import wesseling.io.fasttime.model.CompletedFast
import wesseling.io.fasttime.model.FastingState
import java.util.concurrent.TimeUnit
import wesseling.io.fasttime.util.DateTimeFormatter

/**
 * Manages the fasting timer functionality
 */
class FastingTimer(private val context: Context) {
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
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    
    // Start time in system millis
    private var startTimeMillis: Long = 0
    
    // Shared preferences for persistence
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, 
        Context.MODE_PRIVATE
    )
    
    init {
        // Initialize from shared preferences
        val savedIsRunning = prefs.getBoolean(KEY_IS_RUNNING, false)
        if (savedIsRunning) {
            val savedStartTime = prefs.getLong(KEY_START_TIME, 0)
            if (savedStartTime > 0) {
                isRunning = true
                startTimeMillis = savedStartTime
                elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis
                updateFastingState()
                
                // Restore max fasting state
                val savedMaxState = prefs.getInt(KEY_MAX_FASTING_STATE, 0)
                maxFastingState = FastingState.values()[savedMaxState]
                
                // Start the timer if it's running
                startTimerFromSavedState()
            }
        } else {
            // If not running, get the saved elapsed time
            elapsedTimeMillis = prefs.getLong(KEY_ELAPSED_TIME, 0)
            updateFastingState()
            
            // Restore max fasting state
            val savedMaxState = prefs.getInt(KEY_MAX_FASTING_STATE, 0)
            maxFastingState = FastingState.values()[savedMaxState]
        }
    }
    
    /**
     * Start or restart the timer
     */
    fun startTimer() {
        if (isRunning) return
        
        isRunning = true
        startTimeMillis = System.currentTimeMillis() - elapsedTimeMillis
        
        // Save state to preferences
        saveState()
        
        timerJob = coroutineScope.launch {
            while (isRunning) {
                elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis
                updateFastingState()
                delay(1000) // Update every second
            }
        }
    }
    
    /**
     * Start the timer from a saved state
     */
    private fun startTimerFromSavedState() {
        timerJob = coroutineScope.launch {
            while (isRunning) {
                elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis
                updateFastingState()
                delay(1000) // Update every second
            }
        }
    }
    
    /**
     * Stop the timer
     */
    fun stopTimer() {
        isRunning = false
        timerJob?.cancel()
        timerJob = null
        
        // Save state to preferences
        saveState()
    }
    
    /**
     * Reset the timer to zero and stop it
     * @return CompletedFast object if the timer was running, null otherwise
     */
    fun resetTimer(): CompletedFast? {
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
        
        // Stop the timer
        stopTimer()
        
        // Reset timer values
        elapsedTimeMillis = 0
        currentFastingState = FastingState.NOT_FASTING
        maxFastingState = FastingState.NOT_FASTING
        
        // Save state to preferences
        saveState()
        
        return completedFast
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
        val elapsedHours = TimeUnit.MILLISECONDS.toHours(elapsedTimeMillis)
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
    }
    
    /**
     * Check if notifications are enabled and send a notification for the current fasting state
     */
    private fun checkAndSendFastingStateNotification() {
        // Get preferences manager to check if notifications are enabled
        val preferencesManager = wesseling.io.fasttime.settings.PreferencesManager.getInstance(context)
        val notificationsEnabled = preferencesManager.dateTimePreferences.enableFastingStateNotifications
        
        if (notificationsEnabled) {
            // Create and send notification
            val notificationHelper = wesseling.io.fasttime.notifications.NotificationHelper(context)
            notificationHelper.sendFastingStateNotification(currentFastingState)
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
        prefs.edit().apply {
            putBoolean(KEY_IS_RUNNING, isRunning)
            putLong(KEY_START_TIME, startTimeMillis)
            putLong(KEY_ELAPSED_TIME, elapsedTimeMillis)
            putInt(KEY_MAX_FASTING_STATE, maxFastingState.ordinal)
            apply()
        }
    }
    
    companion object {
        // Shared preferences constants
        const val PREFS_NAME = "wesseling.io.fasttime.TimerPrefs"
        const val KEY_START_TIME = "start_time"
        const val KEY_IS_RUNNING = "is_running"
        const val KEY_ELAPSED_TIME = "elapsed_time"
        const val KEY_MAX_FASTING_STATE = "max_fasting_state"
    }
} 