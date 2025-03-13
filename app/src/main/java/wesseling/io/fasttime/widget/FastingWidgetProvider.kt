package wesseling.io.fasttime.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import wesseling.io.fasttime.MainActivity
import wesseling.io.fasttime.R
import wesseling.io.fasttime.model.FastingState
import wesseling.io.fasttime.timer.FastingTimer
import java.util.concurrent.TimeUnit

/**
 * Implementation of App Widget functionality.
 */
class FastingWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val TAG = "FastingWidgetProvider"
        
        // Actions
        const val ACTION_START_TIMER = "wesseling.io.fasttime.widget.ACTION_START_TIMER"
        const val ACTION_RESET_TIMER = "wesseling.io.fasttime.widget.ACTION_RESET_TIMER"
        const val ACTION_UPDATE_WIDGETS = "wesseling.io.fasttime.widget.ACTION_UPDATE_WIDGETS"
        
        // Update interval in milliseconds (1 minute)
        private const val UPDATE_INTERVAL = 60 * 1000L
        
        /**
         * Update all active widgets
         */
        fun updateAllWidgets(context: Context, forceUpdate: Boolean = false) {
            try {
                Log.d(TAG, "updateAllWidgets called, forceUpdate=$forceUpdate")
            val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(
                    ComponentName(context, FastingWidgetProvider::class.java)
                )
                
                if (appWidgetIds.isNotEmpty()) {
                    Log.d(TAG, "Updating ${appWidgetIds.size} widgets")
                    
                    if (forceUpdate) {
                        // Force update by notifying the AppWidgetManager
                        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_background)
                    }
                    
                    updateAppWidgets(context, appWidgetManager, appWidgetIds)
                } else {
                    Log.d(TAG, "No widgets to update")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in updateAllWidgets", e)
            }
        }
        
        /**
         * Schedule the next widget update
         */
        private fun scheduleNextUpdate(context: Context) {
            val intent = Intent(context, FastingWidgetUpdateService::class.java)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        /**
         * Update all widgets with current fasting state
         */
        private fun updateAppWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            try {
                // Get the fasting timer
                val fastingTimer = FastingTimer.getInstance(context)
                
                // Get current state
                val isRunning = fastingTimer.isRunning
                val currentState = fastingTimer.currentFastingState
                val elapsedHours = TimeUnit.MILLISECONDS.toHours(fastingTimer.elapsedTimeMillis).toInt()
                
                Log.d(TAG, "Updating widgets with state: running=$isRunning, state=${currentState.name}, hours=$elapsedHours")
                
                // Create remote views for each widget
                for (appWidgetId in appWidgetIds) {
                    try {
                        val views = RemoteViews(context.packageName, R.layout.fasting_widget)
                        
                        // Set background color based on fasting state
                        updateWidgetBackground(context, views, currentState, isRunning)
                        
                        // Set hours text
                        views.setTextViewText(R.id.widget_hours, "${elapsedHours}")
                        
                        // Set state text
                        views.setTextViewText(R.id.widget_state, currentState.description)
                        
                        // Set button visibility based on timer state
                        updateWidgetButtons(views, isRunning)
                        
                        // Set button click intents
                        setButtonClickIntents(context, views)
                        
                        // Update the widget
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                        Log.d(TAG, "Widget $appWidgetId updated successfully")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating widget $appWidgetId", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating widgets", e)
            }
        }
        
        /**
         * Update widget background based on fasting state
         */
        private fun updateWidgetBackground(
            context: Context,
            views: RemoteViews,
            currentState: FastingState,
            isRunning: Boolean
        ) {
            try {
                Log.d(TAG, "Setting widget background for state: ${currentState.name}, running: $isRunning")
                
                // Get the color for the current fasting state
                val stateColor = WidgetBackgroundHelper.getColorForFastingState(currentState, context)
                
                // Set the background color on the ImageView
                // We're using a drawable with corner radius defined in XML
                views.setInt(R.id.widget_background_image, "setColorFilter", stateColor)
                
                // Set a border if running
                if (isRunning) {
                    // Add a green border indicator for running state
                    views.setInt(R.id.widget_background, "setBackgroundResource", R.drawable.running_border)
                } else {
                    // Use a transparent background with the same corner radius
                    views.setInt(R.id.widget_background, "setBackgroundResource", R.drawable.widget_container_background)
                }
                
                // Periodically clean up old cache files based on time
                val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
                val lastCleanupTime = prefs.getLong("last_cache_cleanup", 0)
                val currentTime = System.currentTimeMillis()
                val oneDayInMillis = 24 * 60 * 60 * 1000L
                
                // Clean up cache once per day
                if (currentTime - lastCleanupTime > oneDayInMillis) {
                    Log.d(TAG, "Performing scheduled cache cleanup")
                    WidgetBackgroundHelper.cleanupCacheFiles(context)
                    
                    // Update the last cleanup time
                    prefs.edit().putLong("last_cache_cleanup", currentTime).apply()
                }
                
                Log.d(TAG, "Set background color for state: ${currentState.name}, color: ${Integer.toHexString(stateColor)}")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting background", e)
                // Fallback to a safe default
                views.setInt(R.id.widget_background_image, "setColorFilter", Color.DKGRAY)
            }
        }
        
        /**
         * Update widget buttons based on timer state
         */
        private fun updateWidgetButtons(views: RemoteViews, isRunning: Boolean) {
            try {
                if (isRunning) {
                    views.setViewVisibility(R.id.widget_start_button, android.view.View.GONE)
                    views.setViewVisibility(R.id.widget_reset_button, android.view.View.VISIBLE)
            } else {
                    views.setViewVisibility(R.id.widget_start_button, android.view.View.VISIBLE)
                    views.setViewVisibility(R.id.widget_reset_button, android.view.View.GONE)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating button visibility", e)
            }
        }
        
        /**
         * Set button click intents for the widget
         */
        private fun setButtonClickIntents(context: Context, views: RemoteViews) {
            try {
                // Get the fasting timer to check state
                val fastingTimer = FastingTimer.getInstance(context)
                val isRunning = fastingTimer.isRunning
                
                // Create intents for the buttons
                val startIntent = Intent(context, FastingWidgetProvider::class.java).apply {
                    action = ACTION_START_TIMER
                }
                
                val resetIntent = Intent(context, FastingWidgetProvider::class.java).apply {
                    action = ACTION_RESET_TIMER
                }
                
                // Create pending intents
                val startPendingIntent = PendingIntent.getBroadcast(
                    context,
                    1, // Request code for start
                    startIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
                
                val resetPendingIntent = PendingIntent.getBroadcast(
                    context,
                    2, // Different request code for reset
                    resetIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
                
                // Only set adjust time intent if timer is running
                if (isRunning) {
                    // Create intent for the hours text to open adjust time dialog
                    val adjustTimeIntent = Intent(context, WidgetAdjustTimeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    
                    val adjustTimePendingIntent = PendingIntent.getActivity(
                        context,
                        3, // Different request code for adjust time
                        adjustTimeIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                    )
                    
                    // Set click handler for hours text
                    views.setOnClickPendingIntent(R.id.widget_hours, adjustTimePendingIntent)
                    // Also set click handler on the parent container of the hours text
                    views.setOnClickPendingIntent(R.id.widget_hours_container, adjustTimePendingIntent)
                } else {
                    // If not running, clicking hours will start the main app
                    val mainAppIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    
                    val mainAppPendingIntent = PendingIntent.getActivity(
                        context,
                        4, // Different request code for main app
                        mainAppIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                    )
                    
                    // Set click handler for hours text to open main app
                    views.setOnClickPendingIntent(R.id.widget_hours, mainAppPendingIntent)
                    // Also set click handler on the parent container of the hours text
                    views.setOnClickPendingIntent(R.id.widget_hours_container, mainAppPendingIntent)
                }
                
                // Set the separate intents for each button
                views.setOnClickPendingIntent(R.id.widget_start_button, startPendingIntent)
                views.setOnClickPendingIntent(R.id.widget_reset_button, resetPendingIntent)
            } catch (e: Exception) {
                Log.e(TAG, "Error setting button click intents", e)
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        try {
            Log.d(TAG, "onUpdate called for ${appWidgetIds.size} widgets")
            
            // Update all widgets
            updateAppWidgets(context, appWidgetManager, appWidgetIds)
            
            // Schedule the next update
            scheduleNextUpdate(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error in onUpdate", e)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        try {
            Log.d(TAG, "onReceive: ${intent.action}")

        when (intent.action) {
            ACTION_START_TIMER -> {
                    Log.d(TAG, "Starting timer from widget")
                    val fastingTimer = FastingTimer.getInstance(context)
                    fastingTimer.startTimer()
                    updateAllWidgets(context)
            }
            ACTION_RESET_TIMER -> {
                    Log.d(TAG, "Reset timer action received")
                    // Show confirmation dialog
                val confirmIntent = Intent(context, WidgetConfirmationActivity::class.java)
                confirmIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(confirmIntent)
            }
                ACTION_UPDATE_WIDGETS -> {
                    Log.d(TAG, "Updating widgets from broadcast")
                    // Extract state information if available (for debugging)
                    if (intent.hasExtra("IS_RUNNING")) {
                        val isRunning = intent.getBooleanExtra("IS_RUNNING", false)
                        val stateOrdinal = intent.getIntExtra("CURRENT_STATE", 0)
                        val elapsedTime = intent.getLongExtra("ELAPSED_TIME", 0L)
                        Log.d(TAG, "Widget update broadcast with state: running=$isRunning, state=$stateOrdinal, elapsed=$elapsedTime")
                    }
                    
                    // Force update to ensure background is refreshed
                    val forceUpdate = intent.getBooleanExtra("FORCE_UPDATE", false)
                    updateAllWidgets(context, forceUpdate)
                }
                AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                    Log.d(TAG, "Widget update requested")
                    super.onReceive(context, intent)
                    
                    // Also force update our widgets to ensure they have the correct background
                    updateAllWidgets(context, true)
                }
                else -> super.onReceive(context, intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onReceive", e)
            super.onReceive(context, intent)
        }
    }
    
    override fun onEnabled(context: Context) {
        Log.d(TAG, "onEnabled: First widget added")
        // Start the update service when the first widget is added
        ensureUpdateServiceRunning(context)
    }
    
    /**
     * Ensure the widget update service is running
     */
    private fun ensureUpdateServiceRunning(context: Context) {
        try {
            Log.d(TAG, "Ensuring widget update service is running")
            val intent = Intent(context, FastingWidgetUpdateService::class.java)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
                } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting widget update service", e)
        }
    }
    
    override fun onDisabled(context: Context) {
        // Stop the update service when the last widget is removed
        val intent = Intent(context, FastingWidgetUpdateService::class.java)
        context.stopService(intent)
    }
} 