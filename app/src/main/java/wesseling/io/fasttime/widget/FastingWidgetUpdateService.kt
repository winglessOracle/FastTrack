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
                // Update all widgets
                FastingWidgetProvider.updateAllWidgets(this@FastingWidgetUpdateService)
                
                // Schedule next update
                val fastingTimer = FastingTimer.getInstance(this@FastingWidgetUpdateService)
                val updateInterval = if (fastingTimer.isRunning) {
                    // Update more frequently when timer is running
                    TimeUnit.MINUTES.toMillis(1)
                } else {
                    // Update less frequently when timer is not running
                    TimeUnit.MINUTES.toMillis(15)
                }
                
                handler.postDelayed(this, updateInterval)
            } catch (e: Exception) {
                Log.e(TAG, "Error in update runnable", e)
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
        
        // Create a notification for the foreground service
        val notification = createNotification()
        
        // Start as a foreground service
        startForeground(NOTIFICATION_ID, notification)
        
        // Start the update loop
        handler.post(updateRunnable)
        
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
} 