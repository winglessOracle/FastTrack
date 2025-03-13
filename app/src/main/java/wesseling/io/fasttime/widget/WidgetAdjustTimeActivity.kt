package wesseling.io.fasttime.widget

import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
        
        // Get the fasting timer
        val fastingTimer = FastingTimer.getInstance(applicationContext)
        
        // Check if timer is running - only allow adjustments when running
        if (!fastingTimer.isRunning) {
            Log.w(TAG, "Cannot adjust time: Timer is not running")
            Toast.makeText(
                applicationContext,
                "Cannot adjust time: Timer is not running",
                Toast.LENGTH_SHORT
            ).show()
            finish()
            return
        }
        
        setContent {
            FastTrackTheme {
                // Show the adjust start time dialog
                AdjustStartTimeDialog(
                    currentElapsedTimeMillis = fastingTimer.elapsedTimeMillis,
                    onAdjustTime = { adjustmentMillis ->
                        try {
                            // Apply the adjustment
                            val success = fastingTimer.adjustStartTime(adjustmentMillis)
                            if (!success) {
                                Log.e(TAG, "Failed to adjust start time")
                                Toast.makeText(
                                    applicationContext,
                                    "Could not adjust start time. Please try a different time.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Log.d(TAG, "Successfully adjusted start time by $adjustmentMillis ms")
                                
                                // Show success message
                                Toast.makeText(
                                    applicationContext,
                                    "Start time adjusted successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                
                                // Update all widgets
                                FastingWidgetProvider.updateAllWidgets(applicationContext, true)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error adjusting start time", e)
                            Toast.makeText(
                                applicationContext,
                                "An error occurred while adjusting time",
                                Toast.LENGTH_SHORT
                            ).show()
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