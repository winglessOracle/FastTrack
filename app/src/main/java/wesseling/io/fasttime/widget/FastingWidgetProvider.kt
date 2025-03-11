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
        const val ACTION_TOGGLE_TIMER = "wesseling.io.fasttime.widget.ACTION_TOGGLE_TIMER"
        const val ACTION_UPDATE_WIDGETS = "wesseling.io.fasttime.widget.ACTION_UPDATE_WIDGETS"
        
        // Update interval in milliseconds (1 minute)
        private const val UPDATE_INTERVAL = 60 * 1000L
        
        /**
         * Update all active widgets
         */
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, FastingWidgetProvider::class.java)
            )
            
            if (appWidgetIds.isNotEmpty()) {
                updateAppWidgets(context, appWidgetManager, appWidgetIds)
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
                
                // Create remote views for each widget
                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.fasting_widget)
                    
                    // Set background color based on fasting state
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
                    
                    // Set hours text
                    views.setTextViewText(R.id.widget_hours, "${elapsedHours}")
                    
                    // Set state text
                    views.setTextViewText(R.id.widget_state, currentState.description)
                    
                    // Set button visibility based on timer state
                    if (isRunning) {
                        views.setViewVisibility(R.id.widget_start_button, android.view.View.GONE)
                        views.setViewVisibility(R.id.widget_reset_button, android.view.View.VISIBLE)
                        views.setInt(R.id.widget_reset_button, "setBackgroundResource", R.drawable.rounded_button_red)
                    } else {
                        views.setViewVisibility(R.id.widget_start_button, android.view.View.VISIBLE)
                        views.setViewVisibility(R.id.widget_reset_button, android.view.View.GONE)
                        views.setInt(R.id.widget_start_button, "setBackgroundResource", R.drawable.rounded_button)
                    }
                    
                    // Set button click intents
                    val toggleIntent = Intent(context, FastingWidgetProvider::class.java)
                    toggleIntent.action = ACTION_TOGGLE_TIMER
                    
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        0,
                        toggleIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    
                    // Set the same intent for both buttons
                    views.setOnClickPendingIntent(R.id.widget_start_button, pendingIntent)
                    views.setOnClickPendingIntent(R.id.widget_reset_button, pendingIntent)
                    
                    // Make sure the widget background doesn't have any click intent
                    // This ensures only the buttons respond to clicks
                    
                    // Update the widget
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating widgets", e)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Update each widget
        updateAppWidgets(context, appWidgetManager, appWidgetIds)
        
        // Schedule next update
        scheduleNextUpdate(context)
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        when (intent.action) {
            ACTION_TOGGLE_TIMER -> {
                // Toggle the timer
                val fastingTimer = FastingTimer.getInstance(context)
                
                if (fastingTimer.isRunning) {
                    // Show confirmation dialog
                    val confirmIntent = Intent(context, WidgetConfirmationActivity::class.java)
                    confirmIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(confirmIntent)
                } else {
                    // Start the timer
                    fastingTimer.startTimer()
                    
                    // Update widgets
                    updateAllWidgets(context)
                }
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
        // Start the update service when the first widget is added
        scheduleNextUpdate(context)
    }
    
    override fun onDisabled(context: Context) {
        // Stop the update service when the last widget is removed
        val intent = Intent(context, FastingWidgetUpdateService::class.java)
        context.stopService(intent)
    }
} 