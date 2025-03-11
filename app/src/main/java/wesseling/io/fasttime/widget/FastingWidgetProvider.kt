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
        fun updateAllWidgets(context: Context) {
            try {
                Log.d(TAG, "updateAllWidgets called")
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(
                    ComponentName(context, FastingWidgetProvider::class.java)
                )
                
                if (appWidgetIds.isNotEmpty()) {
                    Log.d(TAG, "Updating ${appWidgetIds.size} widgets")
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
         * Update the widget background based on fasting state
         */
        private fun updateWidgetBackground(
            context: Context,
            views: RemoteViews,
            currentState: FastingState,
            isRunning: Boolean
        ) {
            try {
                // Try to create a dynamic background
                val backgroundPath = WidgetBackgroundHelper.createBackgroundDrawable(context, currentState, isRunning)
                if (backgroundPath != null) {
                    views.setImageViewUri(R.id.widget_background, Uri.parse("file://$backgroundPath"))
                } else {
                    // Fallback to static background
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
                    views.setInt(R.id.widget_background, "setBackgroundResource", backgroundResId)
                }
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

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "onUpdate called for ${appWidgetIds.size} widgets")
        
        // Update each widget
        updateAppWidgets(context, appWidgetManager, appWidgetIds)
        
        // Ensure the update service is running
        ensureUpdateServiceRunning(context)
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        Log.d(TAG, "onReceive: action=${intent.action}, extras=${intent.extras}")
        
        when (intent.action) {
            ACTION_START_TIMER -> {
                Log.d(TAG, "Start timer action received")
                try {
                    // Start the timer
                    val fastingTimer = FastingTimer.getInstance(context)
                    fastingTimer.startTimer()
                    Log.d(TAG, "Timer started successfully")
                    
                    // Show feedback to the user
                    try {
                        val toast = android.widget.Toast.makeText(
                            context,
                            "Timer started",
                            android.widget.Toast.LENGTH_SHORT
                        )
                        toast.show()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error showing toast", e)
                    }
                    
                    // Update widgets
                    updateAllWidgets(context)
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting timer from widget", e)
                }
            }
            
            ACTION_RESET_TIMER -> {
                Log.d(TAG, "Reset timer action received")
                // Show confirmation dialog
                val confirmIntent = Intent(context, WidgetConfirmationActivity::class.java)
                confirmIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(confirmIntent)
            }
            
            ACTION_UPDATE_WIDGETS -> {
                // Update all widgets
                updateAllWidgets(context)
            }
            
            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                // Update widgets
                val appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
                if (appWidgetIds != null) {
                    onUpdate(context, AppWidgetManager.getInstance(context), appWidgetIds)
                }
            }
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