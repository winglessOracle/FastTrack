package wesseling.io.fasttime.ui.components

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
            title = { Text("Confirm Reset") },
            text = { Text("Are you sure you want to reset the timer? This will stop tracking your current fasting period.") },
            confirmButton = {
                Button(
                    onClick = {
                        // Reset the timer and get the completed fast
                        val fast = fastingTimer.resetTimer()
                        showConfirmationDialog = false
                        
                        // Show summary dialog if there was a fast
                        if (fast != null && fast.durationMillis > 0) {
                            completedFast = fast
                            showSummaryDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Reset")
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
                    Text("Cancel")
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
                        "Fasting session saved to log",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    Log.e("FastingTimerButton", "Error saving fast to repository", e)
                    
                    // Show error toast
                    Toast.makeText(
                        context,
                        "Error saving fasting session",
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
                val success = fastingTimer.adjustStartTime(adjustmentMillis)
                if (!success) {
                    // Show a toast message if adjustment failed
                    Toast.makeText(
                        context,
                        "Cannot adjust time: Invalid adjustment value",
                        Toast.LENGTH_SHORT
                    ).show()
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
                text = fastingTimer.getFormattedTime(),
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
                        text = fastingTimer.currentFastingState.description,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = buttonColor,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text(
                        text = "Tap to learn more",
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
                        fastingTimer.startTimer()
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
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.animateContentSize()
                ) {
                    Icon(
                        imageVector = if (fastingTimer.isRunning) Icons.Rounded.Refresh else Icons.Rounded.PlayArrow,
                        contentDescription = if (fastingTimer.isRunning) "Reset" else "Start",
                        modifier = Modifier.size(40.dp),
                        tint = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = if (fastingTimer.isRunning) "Reset" else "Start",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
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
                            contentDescription = "Adjust Time",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "Adjust Start Time",
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