package wesseling.io.fasttime.widget

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import wesseling.io.fasttime.timer.FastingTimer
import wesseling.io.fasttime.ui.components.AdjustStartTimeDialog
import wesseling.io.fasttime.ui.theme.FastTrackTheme

/**
 * Activity that shows the adjust start time dialog when opened from the widget
 */
class WidgetAdjustTimeActivity : ComponentActivity() {
    companion object {
        private const val TAG = "WidgetAdjustTimeActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "Opening adjust start time dialog from widget")
        
        setContent {
            FastTrackTheme {
                // Get the fasting timer
                val fastingTimer = FastingTimer.getInstance(applicationContext)
                
                // Show the adjust start time dialog
                AdjustStartTimeDialog(
                    currentElapsedTimeMillis = fastingTimer.elapsedTimeMillis,
                    onAdjustTime = { adjustmentMillis ->
                        try {
                            // Apply the adjustment
                            val success = fastingTimer.adjustStartTime(adjustmentMillis)
                            if (!success) {
                                Log.e(TAG, "Failed to adjust start time")
                            } else {
                                Log.d(TAG, "Successfully adjusted start time by $adjustmentMillis ms")
                                
                                // Update all widgets
                                FastingWidgetProvider.updateAllWidgets(applicationContext, true)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error adjusting start time", e)
                        } finally {
                            finish()
                        }
                    },
                    onDismiss = {
                        finish()
                    }
                )
            }
        }
    }
} 