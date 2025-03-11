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
                        updateWidgetBackground(views, currentState, isRunning)
                        
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
            views: RemoteViews,
            currentState: FastingState,
            isRunning: Boolean
        ) {
            try {
                Log.d(TAG, "Setting widget background for state: ${currentState.name}, running: $isRunning")
                
                // Get the appropriate background resource based on fasting state
                val backgroundResId = if (isRunning) {
                    // Use combined background with running border
                    when (currentState) {
                        FastingState.NOT_FASTING -> R.drawable.widget_background_not_fasting_running
                        FastingState.EARLY_FAST -> R.drawable.widget_background_early_fast_running
                        FastingState.KETOSIS -> R.drawable.widget_background_ketosis_running
                        FastingState.AUTOPHAGY -> R.drawable.widget_background_autophagy_running
                        FastingState.DEEP_FASTING -> R.drawable.widget_background_deep_fasting_running
                    }
                } else {
                    // Use regular state background
                    when (currentState) {
                        FastingState.NOT_FASTING -> R.drawable.widget_background_not_fasting
                        FastingState.EARLY_FAST -> R.drawable.widget_background_early_fast
                        FastingState.KETOSIS -> R.drawable.widget_background_ketosis
                        FastingState.AUTOPHAGY -> R.drawable.widget_background_autophagy
                        FastingState.DEEP_FASTING -> R.drawable.widget_background_deep_fasting
                    }
                }
                
                // Set the background resource directly on the widget_background RelativeLayout
                views.setInt(R.id.widget_background, "setBackgroundResource", backgroundResId)
                Log.d(TAG, "Set background resource: $backgroundResId for state: ${currentState.name}")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting background", e)
                // Fallback to a safe default
                views.setInt(R.id.widget_background, "setBackgroundColor", Color.DKGRAY)
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
         * Set button click intents
         */
        private fun setButtonClickIntents(context: Context, views: RemoteViews) {
            try {
                val startIntent = Intent(context, FastingWidgetProvider::class.java).apply {
                    action = ACTION_START_TIMER
                    putExtra("REQUEST_CODE", System.currentTimeMillis())
                }
                
                val resetIntent = Intent(context, FastingWidgetProvider::class.java).apply {
                    action = ACTION_RESET_TIMER
                    putExtra("REQUEST_CODE", System.currentTimeMillis())
                }
                
                val startPendingIntent = PendingIntent.getBroadcast(
                    context,
                    1, // Different request code for start
                    startIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
                val resetPendingIntent = PendingIntent.getBroadcast(
                    context,
                    2, // Different request code for reset
                    resetIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                
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