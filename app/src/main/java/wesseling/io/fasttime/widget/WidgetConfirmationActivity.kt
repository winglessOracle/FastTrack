package wesseling.io.fasttime.widget

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import wesseling.io.fasttime.R
import wesseling.io.fasttime.model.CompletedFast
import wesseling.io.fasttime.repository.FastingRepository
import wesseling.io.fasttime.timer.FastingTimer
import wesseling.io.fasttime.ui.components.FastingSummaryDialog
import wesseling.io.fasttime.ui.theme.FastTrackTheme
import wesseling.io.fasttime.util.LocaleHelper

/**
 * Activity that shows a confirmation dialog when stopping the timer from the widget
 */
class WidgetConfirmationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply saved language settings to ensure proper localization
        val languageCode = LocaleHelper.getLanguageCode(this)
        LocaleHelper.updateLocale(this, languageCode)
        
        setContent {
            FastTrackTheme {
                // Remove the full-screen Surface
                var showSummaryDialog by remember { mutableStateOf(false) }
                var completedFast by remember { mutableStateOf<CompletedFast?>(null) }
                val repository = remember { FastingRepository.getInstance(applicationContext) }
                
                // Show confirmation dialog with properties to make it more compact
                ConfirmationDialog(
                    onConfirm = {
                        try {
                            // Reset the timer and get the completed fast
                            val fastingTimer = FastingTimer.getInstance(applicationContext)
                            val fast = fastingTimer.resetTimer()
                            
                            // Show summary dialog only if a valid fast was completed (12+ hours)
                            // The resetTimer method now returns null for fasts under 12 hours
                            if (fast != null) {
                                completedFast = fast
                                showSummaryDialog = true
                            } else {
                                // No fast to show or fast was too short, just finish the activity
                                finish()
                            }
                        } catch (e: Exception) {
                            Log.e("WidgetConfirmation", "Error resetting timer", e)
                            // Make sure to finish even if there's an error
                            finish()
                        }
                    },
                    onDismiss = { 
                        // Make sure to finish the activity
                        finish() 
                    }
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
    
    override fun attachBaseContext(base: Context) {
        // Apply the saved language settings to the base context
        val languageCode = LocaleHelper.getLanguageCode(base)
        val updatedContext = LocaleHelper.updateLocale(base, languageCode)
        super.attachBaseContext(updatedContext)
    }
}

@Composable
fun ConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_title_stop_timer)) },
        text = { Text(stringResource(R.string.dialog_message_stop_timer_confirmation)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.action_yes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_no))
            }
        }
    )
} 