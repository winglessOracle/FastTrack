package wesseling.io.fasttime.widget

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import wesseling.io.fasttime.model.CompletedFast
import wesseling.io.fasttime.repository.FastingRepository
import wesseling.io.fasttime.timer.FastingTimer
import wesseling.io.fasttime.ui.components.FastingSummaryDialog
import wesseling.io.fasttime.ui.theme.FastTrackTheme

/**
 * Activity that shows a confirmation dialog for resetting the fasting timer widget
 */
class WidgetConfirmationActivity : ComponentActivity() {
    private var fastingTimer: FastingTimer? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize FastingTimer
        fastingTimer = FastingTimer.getInstance(this)
        
        setContent {
            FastTrackTheme {
                Surface(color = MaterialTheme.colors.background) {
                    var showSummaryDialog by remember { mutableStateOf(false) }
                    var completedFast by remember { mutableStateOf<CompletedFast?>(null) }
                    val repository = remember { FastingRepository.getInstance(this) }
                    
                    ConfirmationDialog(
                        onConfirm = {
                            try {
                                // Reset the timer and get the completed fast
                                val fast = fastingTimer?.resetTimer()
                                
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
                        },
                        onDismiss = {
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
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Clean up references
        fastingTimer = null
    }
}

@Composable
fun ConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Reset") },
        text = { 
            Text("Are you sure you want to reset the fasting timer? This will stop tracking your current fasting period.") 
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Reset")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(8.dp)
    )
} 