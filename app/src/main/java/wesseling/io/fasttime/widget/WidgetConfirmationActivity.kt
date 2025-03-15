package wesseling.io.fasttime.widget

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import wesseling.io.fasttime.R
import wesseling.io.fasttime.model.CompletedFast
import wesseling.io.fasttime.repository.FastingRepository
import wesseling.io.fasttime.timer.FastingTimer
import wesseling.io.fasttime.ui.components.FastingSummaryDialog
import wesseling.io.fasttime.ui.theme.FastTrackTheme

/**
 * Activity that shows a confirmation dialog when stopping the timer from the widget
 */
class WidgetConfirmationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            FastTrackTheme {
                // Remove the full-screen Surface
                var showSummaryDialog by remember { mutableStateOf(false) }
                var completedFast by remember { mutableStateOf<CompletedFast?>(null) }
                val repository = remember { FastingRepository.getInstance(applicationContext) }
                
                // Show confirmation dialog with properties to make it more compact
                AlertDialog(
                    onDismissRequest = { finish() },
                    title = { Text(getString(R.string.stop_timer)) },
                    text = { Text(getString(R.string.stop_timer_confirmation)) },
                    confirmButton = {
                        Button(
                            onClick = {
                                try {
                                    // Reset the timer and get the completed fast
                                    val fastingTimer = FastingTimer.getInstance(applicationContext)
                                    val fast = fastingTimer.resetTimer()
                                    
                                    // Show summary dialog if there was a fast
                                    if (fast != null && fast.durationMillis > 0) {
                                        completedFast = fast
                                        showSummaryDialog = true
                                    } else {
                                        // No fast to show, just finish the activity
                                        finish()
                                    }
                                } catch (e: Exception) {
                                    Log.e("WidgetConfirmation", "Error resetting timer", e)
                                    finish()
                                }
                            }
                        ) {
                            Text(getString(R.string.yes))
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { finish() }
                        ) {
                            Text(getString(R.string.no))
                        }
                    },
                    // Add properties to make the dialog more compact
                    properties = DialogProperties(
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true,
                        usePlatformDefaultWidth = false
                    ),
                    modifier = Modifier.wrapContentSize()
                )
                
                // Summary dialog
                if (showSummaryDialog && completedFast != null) {
                    FastingSummaryDialog(
                        completedFast = completedFast!!,
                        onSave = { fast ->
                            try {
                                // Save the fast to the repository
                                repository.saveFast(fast)
                            } catch (e: Exception) {
                                Log.e("WidgetConfirmation", "Error saving fast", e)
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
} 