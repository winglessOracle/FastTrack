package wesseling.io.fasttime.ui.components

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import wesseling.io.fasttime.R
import wesseling.io.fasttime.model.CompletedFast
import wesseling.io.fasttime.model.FastingState
import wesseling.io.fasttime.repository.FastingRepository
import wesseling.io.fasttime.timer.FastingTimer
import wesseling.io.fasttime.ui.theme.DeepKetosisGreen
import wesseling.io.fasttime.ui.theme.EarlyFastingYellow
import wesseling.io.fasttime.ui.theme.ExtendedFastMagenta
import wesseling.io.fasttime.ui.theme.GlycogenDepletionOrange
import wesseling.io.fasttime.ui.theme.ImmuneResetPurple
import wesseling.io.fasttime.ui.theme.MetabolicShiftBlue
import wesseling.io.fasttime.ui.theme.NotFastingGray
import wesseling.io.fasttime.ui.theme.getColorForFastingState
import wesseling.io.fasttime.ui.theme.BorderUtils
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

/**
 * A widget button that displays and controls a fasting timer
 */
@Composable
fun FastingTimerButton(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fastingTimer = remember { FastingTimer.getInstance(context) }
    val repository = remember { FastingRepository.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()
    
    // Observe lifecycle events for cleanup
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    // Save state when paused
                    try {
                        Log.d("FastingTimerButton", "Lifecycle ON_PAUSE - saving state")
                    } catch (e: Exception) {
                        Log.e("FastingTimerButton", "Error in lifecycle observer", e)
                    }
                }
                Lifecycle.Event.ON_DESTROY -> {
                    // Clean up when destroyed
                    try {
                        Log.d("FastingTimerButton", "Lifecycle ON_DESTROY - cleaning up")
                    } catch (e: Exception) {
                        Log.e("FastingTimerButton", "Error in lifecycle observer", e)
                    }
                }
                else -> {}
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            Log.d("FastingTimerButton", "DisposableEffect cleanup")
        }
    }
    
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showSummaryDialog by remember { mutableStateOf(false) }
    var showAdjustTimeDialog by remember { mutableStateOf(false) }
    var showFastingInfoDialog by remember { mutableStateOf(false) }
    var completedFast by remember { mutableStateOf<CompletedFast?>(null) }
    
    // Animate color changes based on fasting state
    val buttonColor by animateColorAsState(
        targetValue = getColorForFastingState(fastingTimer.currentFastingState),
        animationSpec = tween(durationMillis = 500),
        label = "ButtonColorAnimation"
    )
    
    // Confirmation dialog
    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text(stringResource(R.string.dialog_title_stop_timer)) },
            text = { Text(stringResource(R.string.dialog_message_stop_timer_confirmation)) },
            confirmButton = {
                Button(
                    onClick = {
                        // Use the widget's confirmation activity to handle the reset
                        try {
                            Log.d("FastingTimerButton", "Launching timer reset confirmation")
                            val confirmIntent = Intent(context, Class.forName("wesseling.io.fasttime.widget.WidgetConfirmationActivity"))
                            confirmIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(confirmIntent)
                            showConfirmationDialog = false
                        } catch (e: Exception) {
                            Log.e("FastingTimerButton", "Error launching confirmation", e)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(stringResource(R.string.widget_stop_timer))
                }
            },
            dismissButton = {
                Button(
                    onClick = { showConfirmationDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    
    // Summary dialog
    if (showSummaryDialog && completedFast != null) {
        FastingSummaryDialog(
            completedFast = completedFast!!,
            onSave = { fast ->
                try {
                    // Save the fast to the repository
                    repository.saveFast(fast)
                    Log.d("FastingTimerButton", "Fast saved to repository: ${fast.id}")
                    
                    // Show a toast message to confirm
                    Toast.makeText(
                        context,
                        context.getString(R.string.toast_fast_saved),
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    Log.e("FastingTimerButton", "Error saving fast to repository", e)
                    
                    // Show error toast
                    Toast.makeText(
                        context,
                        context.getString(R.string.toast_fast_save_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onDismiss = {
                showSummaryDialog = false
                completedFast = null
            }
        )
    }
    
    // Adjust time dialog
    if (showAdjustTimeDialog) {
        AdjustStartTimeDialog(
            currentElapsedTimeMillis = fastingTimer.elapsedTimeMillis,
            onAdjustTime = { adjustmentMillis ->
                coroutineScope.launch {
                    try {
                        // Use Handler to move timer operations off the UI thread
                        val handler = Handler(Looper.getMainLooper())
                        handler.post {
                            try {
                                val success = fastingTimer.adjustStartTime(adjustmentMillis)
                                handler.post {
                                    if (!success) {
                                        // Show a toast message if adjustment failed
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.toast_invalid_adjustment),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("FastingTimerButton", "Error adjusting time", e)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("FastingTimerButton", "Error in adjust time coroutine", e)
                    }
                }
            },
            onDismiss = {
                showAdjustTimeDialog = false
            }
        )
    }
    
    // Fasting state info dialog
    if (showFastingInfoDialog) {
        FastingStateInfoDialog(
            fastingState = fastingTimer.currentFastingState,
            onDismiss = { showFastingInfoDialog = false }
        )
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = buttonColor.copy(alpha = 0.3f)
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Timer display with subtle animation
            Text(
                text = fastingTimer.getFormattedElapsedTime(),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.animateContentSize(
                    animationSpec = tween(durationMillis = 300)
                )
            )
            
            // Fasting state description with enhanced card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { showFastingInfoDialog = true }
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = buttonColor.copy(alpha = 0.2f)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = buttonColor.copy(alpha = 0.15f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = fastingTimer.currentFastingState.getDescription(context),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = buttonColor,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text(
                        text = stringResource(R.string.help_fasting_tips),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = buttonColor.copy(alpha = 0.7f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Timer button with enhanced visual effects
            Button(
                onClick = {
                    if (fastingTimer.isRunning) {
                        showConfirmationDialog = true
                    } else {
                        // Use direct timer control instead of broadcasts to prevent crashes
                        try {
                            Log.d("FastingTimerButton", "Starting timer using safeStartTimer")
                            // Use the safe start timer method that already handles locale
                            fastingTimer.safeStartTimer()
                        } catch (e: Exception) {
                            Log.e("FastingTimerButton", "Error starting timer directly", e)
                            // Fallback to the older method if direct call fails
                            try {
                                Log.d("FastingTimerButton", "Falling back to broadcast timer start")
                                val startIntent = Intent("wesseling.io.fasttime.widget.ACTION_START_TIMER")
                                startIntent.setPackage(context.packageName)
                                context.sendBroadcast(startIntent)
                            } catch (e2: Exception) {
                                Log.e("FastingTimerButton", "Error in broadcast fallback", e2)
                            }
                        }
                    }
                },
                modifier = Modifier
                    .size(130.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = CircleShape,
                        spotColor = buttonColor.copy(alpha = 0.5f)
                    ),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor,
                    contentColor = Color.White
                ),
                border = if (fastingTimer.isRunning) {
                    BorderUtils.createActiveTimerBorder()
                } else {
                    null
                }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.animateContentSize()
                ) {
                    Icon(
                        imageVector = if (fastingTimer.isRunning) Icons.Rounded.Refresh else Icons.Rounded.PlayArrow,
                        contentDescription = if (fastingTimer.isRunning) stringResource(R.string.widget_stop_timer) else stringResource(R.string.widget_start_timer),
                        modifier = Modifier.size(40.dp),
                        tint = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = if (fastingTimer.isRunning) stringResource(R.string.widget_stop_timer) else stringResource(R.string.widget_start_timer),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // Adjust time button with improved styling
            if (fastingTimer.isRunning) {
                TextButton(
                    onClick = { showAdjustTimeDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccessTime,
                            contentDescription = stringResource(R.string.action_adjust_time),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = stringResource(R.string.action_adjust_time),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
} 
