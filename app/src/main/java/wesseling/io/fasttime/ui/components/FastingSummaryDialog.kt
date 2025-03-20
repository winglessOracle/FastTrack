package wesseling.io.fasttime.ui.components

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import wesseling.io.fasttime.R
import wesseling.io.fasttime.model.CompletedFast
import wesseling.io.fasttime.model.FastingState
import wesseling.io.fasttime.ui.theme.getColorForFastingState

/**
 * Dialog that shows a summary of a completed fast and offers to save it
 */
@Composable
fun FastingSummaryDialog(
    completedFast: CompletedFast,
    onSave: (CompletedFast) -> Unit,
    onDismiss: () -> Unit
) {
    var note by remember { mutableStateOf(completedFast.note) }
    val context = LocalContext.current
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = stringResource(R.string.fasting_log_summary_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Fasting state with color
                val stateColor = getColorForFastingState(completedFast.maxFastingState)
                Text(
                    text = completedFast.maxFastingState.getDisplayName(context),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = stateColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(stateColor.copy(alpha = 0.1f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Duration in large text
                Text(
                    text = formatDuration(context, completedFast.durationMillis),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Achievement badge
                if (completedFast.maxFastingState != FastingState.NOT_FASTING && 
                    completedFast.maxFastingState != FastingState.EARLY_FAST) {
                    val achievementColor = getColorForFastingState(completedFast.maxFastingState)
                    
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(achievementColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Achievement",
                            tint = achievementColor,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = stringResource(R.string.fasting_log_achievement_reached, completedFast.maxFastingState.description),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = achievementColor,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Fast details
                FastDetailRow(
                    icon = Icons.Filled.AccessTime,
                    label = stringResource(R.string.fasting_log_duration),
                    value = completedFast.getFormattedDuration()
                )
                
                FastDetailRow(
                    icon = Icons.Filled.CalendarToday,
                    label = stringResource(R.string.fasting_log_started),
                    value = completedFast.getFormattedStartTime(context)
                )
                
                FastDetailRow(
                    icon = Icons.Filled.CheckCircle,
                    label = stringResource(R.string.fasting_log_ended),
                    value = completedFast.getFormattedEndTime(context)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Notes field
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.fasting_log_notes)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(stringResource(R.string.fasting_log_discard))
                    }
                    
                    Button(
                        onClick = {
                            try {
                                // Create a copy with the updated note
                                val fastWithNote = completedFast.copy(note = note)
                                
                                // Call the onSave callback
                                onSave(fastWithNote)
                                
                                // Only dismiss if no exception was thrown
                                onDismiss()
                            } catch (e: Exception) {
                                Log.e("FastingSummaryDialog", "Error saving fast: ${e.message}", e)
                                // Don't dismiss on error so user can try again
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(stringResource(R.string.fasting_log_save))
                    }
                }
            }
        }
    }
}

@Composable
private fun FastDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = label,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// Helper functions for formatting time
private fun formatDuration(context: Context, durationMillis: Long): String {
    val seconds = durationMillis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    
    return when {
        hours > 0 -> context.getString(R.string.format_hours_minutes_detailed, hours, minutes % 60)
        minutes > 0 -> context.getString(R.string.format_minutes_only_detailed, minutes)
        else -> context.getString(R.string.format_seconds_only_detailed, seconds)
    }
}

private fun formatDateTime(timeMillis: Long): String {
    val dateFormat = java.text.SimpleDateFormat("MMM d, yyyy 'at' h:mm a", java.util.Locale.getDefault())
    return dateFormat.format(java.util.Date(timeMillis))
} 