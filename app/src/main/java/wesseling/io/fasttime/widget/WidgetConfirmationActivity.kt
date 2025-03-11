package wesseling.io.fasttime.widget

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import wesseling.io.fasttime.MainActivity
import wesseling.io.fasttime.R
import wesseling.io.fasttime.model.CompletedFast
import wesseling.io.fasttime.timer.FastingTimer

/**
 * Activity that shows a confirmation dialog when stopping the timer from the widget
 */
class WidgetConfirmationActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Show confirmation dialog
        AlertDialog.Builder(this)
            .setTitle(R.string.stop_timer)
            .setMessage(R.string.stop_timer_confirmation)
            .setPositiveButton(R.string.yes) { _, _ ->
                try {
                    // Reset the timer and get the completed fast
                    val fastingTimer = FastingTimer.getInstance(applicationContext)
                    val completedFast = fastingTimer.resetTimer()
                    
                    // If there was a fast, start the main activity to show the summary
                    if (completedFast != null && completedFast.durationMillis > 0) {
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("SHOW_COMPLETED_FAST", true)
                        intent.putExtra("START_TIME", completedFast.startTimeMillis)
                        intent.putExtra("END_TIME", completedFast.endTimeMillis)
                        intent.putExtra("DURATION", completedFast.durationMillis)
                        intent.putExtra("MAX_STATE", completedFast.maxFastingState.ordinal)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                    }
                } catch (e: Exception) {
                    Log.e("WidgetConfirmation", "Error resetting timer", e)
                } finally {
                    finish()
                }
            }
            .setNegativeButton(R.string.no) { _, _ ->
                // Just close the activity
                finish()
            }
            .setOnCancelListener {
                // Just close the activity
                finish()
            }
            .show()
    }
} 