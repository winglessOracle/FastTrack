package wesseling.io.fasttime.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.widget.RemoteViews
import wesseling.io.fasttime.MainActivity
import wesseling.io.fasttime.R
import wesseling.io.fasttime.model.FastingState
import wesseling.io.fasttime.timer.FastingTimer
import java.util.concurrent.TimeUnit

/**
 * Implementation of Large App Widget functionality.
 */
class FastingWidgetLargeProvider : AppWidgetProvider() {

    companion object {
        private const val TAG = "FastingWidgetLargeProvider"
        
        // Actions - reusing the same actions as the regular widget
        private const val ACTION_START_TIMER = FastingWidgetProvider.ACTION_START_TIMER
        private const val ACTION_RESET_TIMER = FastingWidgetProvider.ACTION_RESET_TIMER
        private const val ACTION_UPDATE_WIDGETS = FastingWidgetProvider.ACTION_UPDATE_WIDGETS
        
        /**
         * Update all active large widgets
         */
        fun updateAllWidgets(context: Context, forceUpdate: Boolean = false) {
            try {
                Log.d(TAG, "updateAllWidgets called, forceUpdate=$forceUpdate")
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(
                    ComponentName(context, FastingWidgetLargeProvider::class.java)
                )
                
                if (appWidgetIds.isNotEmpty()) {
                    Log.d(TAG, "Updating ${appWidgetIds.size} large widgets")
                    
                    if (forceUpdate) {
                        // Force update by notifying the AppWidgetManager
                        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_background_large)
                    }
                    
                    updateAppWidgets(context, appWidgetManager, appWidgetIds)
                } else {
                    Log.d(TAG, "No large widgets to update")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in updateAllWidgets", e)
            }
        }
        
        /**
         * Update all large widgets with current fasting state
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
                
                Log.d(TAG, "Updating large widgets with state: running=$isRunning, state=${currentState.name}, hours=$elapsedHours")
                
                // Create remote views for each widget
                for (appWidgetId in appWidgetIds) {
                    try {
                        val views = RemoteViews(context.packageName, R.layout.fasting_widget_large)
                        
                        // Set background color based on fasting state
                        updateWidgetBackground(context, views, currentState, isRunning)
                        
                        // Set hours text
                        views.setTextViewText(R.id.widget_hours_large, "${elapsedHours}")
                        
                        // Set state text
                        views.setTextViewText(R.id.widget_state_large, currentState.description)
                        
                        // Set button visibility based on timer state
                        updateWidgetButtons(views, isRunning)
                        
                        // Set button click intents
                        setButtonClickIntents(context, views)
                        
                        // Update the widget
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                        Log.d(TAG, "Large widget $appWidgetId updated successfully")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating large widget $appWidgetId", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating large widgets", e)
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
                Log.d(TAG, "Setting large widget background for state: ${currentState.name}, running: $isRunning")
                
                // Get the color for the current fasting state
                val stateColor = WidgetBackgroundHelper.getColorForFastingState(currentState, context)
                
                // Set the background color on the ImageView
                views.setInt(R.id.widget_background_image_large, "setColorFilter", stateColor)
                
                // Set a border if running
                if (isRunning) {
                    // Add a green border indicator for running state
                    views.setInt(R.id.widget_background_large, "setBackgroundResource", R.drawable.running_border)
                } else {
                    // Use a transparent background with the same corner radius
                    views.setInt(R.id.widget_background_large, "setBackgroundResource", R.drawable.widget_container_background)
                }
                
                Log.d(TAG, "Set background color for state: ${currentState.name}, color: ${Integer.toHexString(stateColor)}")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting background", e)
                // Fallback to a safe default
                views.setInt(R.id.widget_background_image_large, "setColorFilter", Color.DKGRAY)
            }
        }
        
        /**
         * Update widget buttons based on timer state
         */
        private fun updateWidgetButtons(views: RemoteViews, isRunning: Boolean) {
            try {
                if (isRunning) {
                    views.setViewVisibility(R.id.widget_start_button_large, android.view.View.GONE)
                    views.setViewVisibility(R.id.widget_reset_button_large, android.view.View.VISIBLE)
                } else {
                    views.setViewVisibility(R.id.widget_start_button_large, android.view.View.VISIBLE)
                    views.setViewVisibility(R.id.widget_reset_button_large, android.view.View.GONE)
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
                val currentState = fastingTimer.currentFastingState
                
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
                    101, // Different request code for large widget start
                    startIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
                
                val resetPendingIntent = PendingIntent.getBroadcast(
                    context,
                    102, // Different request code for large widget reset
                    resetIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
                
                // Create intent for the state pill to open state info dialog
                val stateInfoIntent = Intent(context, FastingStateInfoActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(FastingStateInfoActivity.EXTRA_FASTING_STATE_ORDINAL, currentState.ordinal)
                }
                
                val stateInfoPendingIntent = PendingIntent.getActivity(
                    context,
                    105, // Different request code for large widget state info
                    stateInfoIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )
                
                // Set click handler for state pill
                views.setOnClickPendingIntent(R.id.widget_state_container_large, stateInfoPendingIntent)
                
                // Only set adjust time intent if timer is running
                if (isRunning) {
                    // Create intent for the hours text to open adjust time dialog
                    val adjustTimeIntent = Intent(context, WidgetAdjustTimeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    
                    val adjustTimePendingIntent = PendingIntent.getActivity(
                        context,
                        103, // Different request code for large widget adjust time
                        adjustTimeIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                    )
                    
                    // Set click handler for hours text
                    views.setOnClickPendingIntent(R.id.widget_hours_large, adjustTimePendingIntent)
                    // Also set click handler on the parent container of the hours text
                    views.setOnClickPendingIntent(R.id.widget_hours_container_large, adjustTimePendingIntent)
                } else {
                    // If not running, clicking hours will start the main app
                    val mainAppIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    
                    val mainAppPendingIntent = PendingIntent.getActivity(
                        context,
                        104, // Different request code for large widget main app
                        mainAppIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                    )
                    
                    // Set click handler for hours text to open main app
                    views.setOnClickPendingIntent(R.id.widget_hours_large, mainAppPendingIntent)
                    // Also set click handler on the parent container of the hours text
                    views.setOnClickPendingIntent(R.id.widget_hours_container_large, mainAppPendingIntent)
                }
                
                // Set the separate intents for each button
                views.setOnClickPendingIntent(R.id.widget_start_button_large, startPendingIntent)
                views.setOnClickPendingIntent(R.id.widget_reset_button_large, resetPendingIntent)
            } catch (e: Exception) {
                Log.e(TAG, "Error setting button click intents", e)
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        try {
            Log.d(TAG, "onUpdate called for ${appWidgetIds.size} large widgets")
            
            // Update all widgets
            updateAppWidgets(context, appWidgetManager, appWidgetIds)
            
            // Schedule the next update using the existing service
            val intent = Intent(context, FastingWidgetUpdateService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onUpdate", e)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        try {
            Log.d(TAG, "onReceive: ${intent.action}")

            when (intent.action) {
                ACTION_UPDATE_WIDGETS -> {
                    Log.d(TAG, "Updating large widgets from broadcast")
                    // Force update to ensure background is refreshed
                    val forceUpdate = intent.getBooleanExtra("FORCE_UPDATE", false)
                    updateAllWidgets(context, forceUpdate)
                }
                AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                    Log.d(TAG, "Large widget update requested")
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
        Log.d(TAG, "onEnabled: First large widget added")
        // Ensure the update service is running
        val intent = Intent(context, FastingWidgetUpdateService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
} 