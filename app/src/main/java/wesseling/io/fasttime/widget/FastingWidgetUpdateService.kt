package wesseling.io.fasttime.widget

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import wesseling.io.fasttime.MainActivity
import wesseling.io.fasttime.R
import wesseling.io.fasttime.timer.FastingTimer
import java.util.concurrent.TimeUnit

/**
 * Service to update the fasting widget periodically
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
     * Schedule the next update with an adaptive interval based on fasting state
     */
    private fun scheduleNextUpdateWithAdaptiveInterval() {
        try {
            val fastingTimer = FastingTimer.getInstance(this)
            
            // Calculate appropriate update interval
            val updateInterval = when {
                // When timer is running, update more frequently
                fastingTimer.isRunning -> {
                    val elapsedHours = TimeUnit.MILLISECONDS.toHours(fastingTimer.elapsedTimeMillis).toInt()
                    
                    // Update more frequently near state transitions
                    when {
                        // Near state transitions (within 10 minutes), update more frequently
                        isNearStateTransition(elapsedHours) -> TimeUnit.SECONDS.toMillis(30)
                        
                        // First hour of fasting, update every minute
                        elapsedHours < 1 -> TimeUnit.MINUTES.toMillis(1)
                        
                        // After 24 hours, update less frequently
                        elapsedHours >= 24 -> TimeUnit.MINUTES.toMillis(5)
                        
                        // Default update interval when running
                        else -> TimeUnit.MINUTES.toMillis(2)
                    }
                }
                
                // When not running, update infrequently to save battery
                else -> TimeUnit.MINUTES.toMillis(15)
            }
            
            Log.d(TAG, "Next update scheduled in ${updateInterval/1000} seconds")
            
            // Ensure we don't have multiple callbacks
            handler.removeCallbacks(updateRunnable)
            handler.postDelayed(updateRunnable, updateInterval)
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling next update", e)
            // Fallback to a safe interval
            handler.postDelayed(updateRunnable, TimeUnit.MINUTES.toMillis(5))
        }
    }
    
    /**
     * Check if the current elapsed time is near a state transition
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