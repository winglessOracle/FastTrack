package wesseling.io.fasttime.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.concurrent.TimeUnit
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import wesseling.io.fasttime.settings.PreferencesManager
import wesseling.io.fasttime.util.DateTimeFormatter
// Import the picker components
import wesseling.io.fasttime.ui.components.DayPicker
import wesseling.io.fasttime.ui.components.NumberPicker
import wesseling.io.fasttime.model.TimeFormat

/**
 * Data class to hold time-related state
 */
private data class TimeState(
    val hour: Int,
    val minute: Int,
    val dayIndex: Int,
    val isAM: Boolean
)

/**
 * Data class to represent validation errors
 */
private data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

/**
 * Converts a 12-hour format hour to 24-hour format
 * 
 * @param hour12 The hour in 12-hour format (1-12)
 * @param isAM Whether the time is AM (true) or PM (false)
 * @return The hour in 24-hour format (0-23)
 */
private fun convertTo24Hour(hour12: Int, isAM: Boolean): Int {
    return when {
        hour12 == 12 && isAM -> 0      // 12 AM -> 0
        hour12 == 12 && !isAM -> 12    // 12 PM -> 12
        !isAM -> hour12 + 12           // 1-11 PM -> 13-23
        else -> hour12                  // 1-11 AM -> 1-11
    }
}

/**
 * Reusable error message component
 */
@Composable
private fun ErrorMessage(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Error,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Dialog for adjusting the start time of a fast
 */
@Composable
fun AdjustStartTimeDialog(
    currentElapsedTimeMillis: Long,
    onAdjustTime: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    // Constants
    val MAX_DAYS_BACK = 30
    val MAX_ADJUSTMENT_MILLIS = TimeUnit.DAYS.toMillis(MAX_DAYS_BACK.toLong())
    
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
    
    // Determine if we should use 12-hour format based on user preferences
    val use12HourFormat = preferences.timeFormat == TimeFormat.HOURS_12
    
    // Create a list of selectable days (today and previous days)
    val daysList = remember(currentStartTimeMillis) {
        (0..MAX_DAYS_BACK).map { daysBack ->
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
        day == calendar.get(Calendar.DAY_OF_MONTH) && 
        month == calendar.get(Calendar.MONTH) && 
        year == calendar.get(Calendar.YEAR)
    }.coerceAtLeast(0)
    
    // Initialize time state
    var timeState by remember { 
        mutableStateOf(
            TimeState(
                hour = calendar.get(Calendar.HOUR_OF_DAY),
                minute = calendar.get(Calendar.MINUTE),
                dayIndex = initialDayIndex,
                isAM = calendar.get(Calendar.AM_PM) == Calendar.AM
            )
        ) 
    }
    
    // Convert 24-hour format to 12-hour format for display if needed
    val displayHour = if (use12HourFormat) {
        val h = timeState.hour % 12
        if (h == 0) 12 else h  // Convert 0 to 12 for 12-hour format
    } else {
        timeState.hour
    }
    
    // Get the selected day components
    val (selectedDayOfMonth, selectedMonth, selectedYear) = daysList[timeState.dayIndex]
    
    // Calculate the new start time based on selected date and time
    val newStartCalendar = Calendar.getInstance().apply {
        set(selectedYear, selectedMonth, selectedDayOfMonth, timeState.hour, timeState.minute, 0)
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
    
    // Validate the adjustment
    val validation = validateAdjustment(
        newStartTimeMillis = newStartTimeMillis,
        newElapsedTimeMillis = newElapsedTimeMillis,
        adjustmentMillis = adjustmentMillis,
        maxAdjustmentMillis = MAX_ADJUSTMENT_MILLIS
    )
    
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
                                selectedIndex = timeState.dayIndex,
                                onIndexChange = { 
                                    timeState = timeState.copy(dayIndex = it)
                                },
                                daysList = daysList,
                                hasError = !validation.isValid && validation.errorMessage?.contains("future") == true
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
                                    value = displayHour,
                                    onValueChange = { newHour ->
                                        if (use12HourFormat) {
                                            // Convert 12-hour format to 24-hour format
                                            val newHour24 = convertTo24Hour(newHour, timeState.isAM)
                                            timeState = timeState.copy(hour = newHour24)
                                        } else {
                                            timeState = timeState.copy(hour = newHour)
                                        }
                                    },
                                    range = if (use12HourFormat) 1..12 else 0..23,
                                    format = { 
                                        if (use12HourFormat) {
                                            "%d".format(it)  // No leading zero for 12-hour format
                                        } else {
                                            "%02d".format(it) // Leading zero for 24-hour format
                                        }
                                    },
                                    hasError = !validation.isValid
                                )
                                
                                Text(
                                    text = ":",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                
                                // Minute picker
                                NumberPicker(
                                    value = timeState.minute,
                                    onValueChange = { 
                                        timeState = timeState.copy(minute = it)
                                    },
                                    range = 0..59,
                                    format = { "%02d".format(it) },
                                    hasError = !validation.isValid
                                )
                                
                                // AM/PM selector for 12-hour format
                                if (use12HourFormat) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    NumberPicker(
                                        value = if (timeState.isAM) 0 else 1,
                                        onValueChange = { 
                                            val newIsAM = it == 0
                                            // Update the 24-hour hour based on AM/PM change
                                            val newHour24 = convertTo24Hour(displayHour, newIsAM)
                                            timeState = timeState.copy(hour = newHour24, isAM = newIsAM)
                                        },
                                        range = 0..1,
                                        format = { if (it == 0) "AM" else "PM" },
                                        hasError = !validation.isValid
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Show the selected date and time
                    Text(
                        text = "New start time: $newStartTimeFormatted",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (validation.isValid) 
                            MaterialTheme.colorScheme.onSurfaceVariant 
                        else 
                            MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    
                    // Display error message if validation failed
                    if (!validation.isValid && validation.errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        ErrorMessage(validation.errorMessage)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // New time display
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (validation.isValid && adjustmentMillis != 0L)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
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
                            color = if (validation.isValid && adjustmentMillis != 0L)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = newFormattedTime,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (validation.isValid && adjustmentMillis != 0L)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
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
                enabled = validation.isValid && adjustmentMillis != 0L,
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

/**
 * Validates the adjustment parameters and returns a validation result
 */
private fun validateAdjustment(
    newStartTimeMillis: Long,
    newElapsedTimeMillis: Long,
    adjustmentMillis: Long,
    maxAdjustmentMillis: Long
): ValidationResult {
    val currentTime = System.currentTimeMillis()
    
    return when {
        newStartTimeMillis > currentTime -> ValidationResult(
            isValid = false,
            errorMessage = "Start time cannot be in the future. Please select an earlier time."
        )
        newElapsedTimeMillis <= 0 -> ValidationResult(
            isValid = false,
            errorMessage = "This would result in negative fasting time. Please select a later time."
        )
        adjustmentMillis >= maxAdjustmentMillis -> ValidationResult(
            isValid = false,
            errorMessage = "Adjustment is too large (maximum 30 days). Please select a more recent date."
        )
        adjustmentMillis == 0L -> ValidationResult(
            isValid = true,
            errorMessage = "No change to current time. Adjust to apply changes."
        )
        else -> ValidationResult(isValid = true)
    }
}
