package wesseling.io.fasttime.widget

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.res.stringResource
import wesseling.io.fasttime.R
import wesseling.io.fasttime.timer.FastingTimer
import wesseling.io.fasttime.ui.components.AdjustStartTimeDialog
import wesseling.io.fasttime.ui.theme.FastTrackTheme
import wesseling.io.fasttime.util.LocaleHelper

/**
 * Activity for adjusting the start time of a fast from the widget
 */
class WidgetAdjustTimeActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "WidgetAdjustTimeActivity"
    }
    
    override fun attachBaseContext(base: Context) {
        // Apply the saved language settings to the base context
        val languageCode = LocaleHelper.getLanguageCode(base)
        val updatedContext = LocaleHelper.updateLocale(base, languageCode)
        super.attachBaseContext(updatedContext)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply saved language settings to ensure proper localization
        val languageCode = LocaleHelper.getLanguageCode(this)
        LocaleHelper.updateLocale(this, languageCode)
        
        Log.d(TAG, "Opening adjust start time dialog from widget")
        
        // Get the fasting timer
        val fastingTimer = FastingTimer.getInstance(applicationContext)
        
        // Check if timer is running - only allow adjustments when running
        if (!fastingTimer.isRunning) {
            Log.w(TAG, "Cannot adjust time: Timer is not running")
            Toast.makeText(
                applicationContext,
                getString(R.string.toast_timer_not_running),
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
                                    getString(R.string.toast_invalid_time_adjustment),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Log.d(TAG, "Successfully adjusted start time by $adjustmentMillis ms")
                                
                                // Show success message
                                Toast.makeText(
                                    applicationContext,
                                    getString(R.string.toast_time_adjusted),
                                    Toast.LENGTH_SHORT
                                ).show()
                                
                                // Update all widgets
                                FastingWidgetProvider.updateAllWidgets(applicationContext, true)
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error adjusting start time", e)
                            Toast.makeText(
                                applicationContext,
                                getString(R.string.toast_adjustment_error),
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