package wesseling.io.fasttime.ui.components

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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.concurrent.TimeUnit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar
import wesseling.io.fasttime.model.DateTimePreferences
import wesseling.io.fasttime.settings.PreferencesManager
import wesseling.io.fasttime.util.DateTimeFormatter
// Import the picker components from the Pickers.kt file
import wesseling.io.fasttime.ui.components.DayPicker
import wesseling.io.fasttime.ui.components.NumberPicker

/**
 * Dialog for adjusting the start time of a fast
 */
@Composable
fun AdjustStartTimeDialog(
    currentElapsedTimeMillis: Long,
    onAdjustTime: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    // Get preferences manager
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager.getInstance(context) }
    val preferences = preferencesManager.dateTimePreferences
    
    // Calculate current start time
    val currentStartTimeMillis = System.currentTimeMillis() - currentElapsedTimeMillis
    
    // Extract date and time components from the start time
    val calendar = Calendar.getInstance().apply {
        timeInMillis = currentStartTimeMillis
    }
    
    // Initialize time picker with the start time
    var selectedHour by remember { mutableIntStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableIntStateOf(calendar.get(Calendar.MINUTE)) }
    
    // Current date components
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
    
    // Calculate the maximum number of days we can go back
    // (limit to 30 days in the past to prevent unreasonable adjustments)
    val maxDaysBack = 30
    
    // Create a list of selectable days (today and previous days)
    val daysList = remember {
        (0..maxDaysBack).map { daysBack ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_MONTH, -daysBack)
            Triple(
                cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.YEAR)
            )
        }
    }
    
    // Find the index of the current day in the list
    val initialDayIndex = daysList.indexOfFirst { (day, month, year) ->
        day == currentDay && month == currentMonth && year == currentYear
    }.coerceAtLeast(0)
    
    // State for the selected day index
    var selectedDayIndex by remember { mutableIntStateOf(initialDayIndex) }
    
    // Get the selected day components
    val (selectedDayOfMonth, selectedMonth, selectedYear) = daysList[selectedDayIndex]
    
    // Calculate the new start time based on selected date and time
    val newStartCalendar = Calendar.getInstance().apply {
        set(selectedYear, selectedMonth, selectedDayOfMonth, selectedHour, selectedMinute, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val newStartTimeMillis = newStartCalendar.timeInMillis
    
    // Calculate adjustment in milliseconds
    val adjustmentMillis = currentStartTimeMillis - newStartTimeMillis
    
    // Calculate new elapsed time
    val newElapsedTimeMillis = currentElapsedTimeMillis + adjustmentMillis
    
    // Format times for display
    val currentFormattedTime = DateTimeFormatter.formatElapsedTime(currentElapsedTimeMillis)
    val newFormattedTime = DateTimeFormatter.formatElapsedTime(newElapsedTimeMillis)
    
    // Format start times as actual dates
    val currentStartTimeFormatted = DateTimeFormatter.formatDateTime(currentStartTimeMillis, preferences)
    val newStartTimeFormatted = DateTimeFormatter.formatDateTime(newStartTimeMillis, preferences)
    
    // Determine if the adjustment is valid (not in the future)
    val currentTime = System.currentTimeMillis()
    val isValidAdjustment = newStartTimeMillis <= currentTime
    
    // Determine if the adjustment is valid for fasting (not negative elapsed time)
    val isValidForFasting = newElapsedTimeMillis > 0
    
    // Check if the adjustment is reasonable (not too large)
    val isReasonableAdjustment = adjustmentMillis < TimeUnit.DAYS.toMillis(30)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.AccessTime,
                    contentDescription = "Clock Icon",
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Adjust Start Time",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "If you forgot to start the timer when you began fasting, you can adjust the start time here.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Current time display
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Current Elapsed Time",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = currentFormattedTime,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Started fasting on:",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Text(
                            text = currentStartTimeFormatted,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Time picker
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Set New Start Time",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Date and time pickers in a row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Day picker
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Day",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            DayPicker(
                                selectedIndex = selectedDayIndex,
                                onIndexChange = { selectedDayIndex = it },
                                daysList = daysList
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // Time pickers
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Time",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Hour picker
                                NumberPicker(
                                    value = selectedHour,
                                    onValueChange = { selectedHour = it },
                                    range = 0..23,
                                    format = { "%02d".format(it) }
                                )
                                
                                Text(
                                    text = ":",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                
                                // Minute picker
                                NumberPicker(
                                    value = selectedMinute,
                                    onValueChange = { selectedMinute = it },
                                    range = 0..59,
                                    format = { "%02d".format(it) }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Show the selected date and time
                    Text(
                        text = "New start time: $newStartTimeFormatted",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    if (!isValidAdjustment) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Start time cannot be in the future",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else if (!isValidForFasting) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Adjustment would result in negative fasting time",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else if (!isReasonableAdjustment) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Adjustment is too large (maximum 30 days)",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // New time display
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "New Elapsed Time",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = newFormattedTime,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdjustTime(adjustmentMillis)
                    onDismiss()
                },
                enabled = isValidAdjustment && isValidForFasting && 
                          adjustmentMillis != 0L && isReasonableAdjustment,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Apply Adjustment")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
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
