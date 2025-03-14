package wesseling.io.fasttime.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * A day picker component that allows scrolling through days
 * 
 * @param selectedIndex The currently selected index in the daysList
 * @param onIndexChange Callback when the index changes
 * @param daysList List of days as Triple<day, month, year>
 * @param hasError Whether to show an error state
 */
@Composable
fun DayPicker(
    selectedIndex: Int,
    onIndexChange: (Int) -> Unit,
    daysList: List<Triple<Int, Int, Int>>,
    hasError: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(90.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .then(
                if (hasError) {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier
                }
            )
            .padding(vertical = 8.dp)
    ) {
        // Up arrow
        IconButton(
            onClick = {
                val newIndex = if (selectedIndex <= 0) 0 else selectedIndex - 1
                onIndexChange(newIndex)
            },
            modifier = Modifier.size(48.dp) // Larger touch target
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowDropUp,
                contentDescription = "Select previous day", // More descriptive
                tint = if (hasError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
        
        // Current value
        Box(
            modifier = Modifier
                .height(40.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val (day, month, year) = daysList[selectedIndex]
            
            // Create a formatted date string that includes month info
            val dateStr = SimpleDateFormat("dd MMM", Locale.getDefault()).format(
                Calendar.getInstance().apply {
                    set(year, month, day)
                }.time
            )
            
            // Format as "DD MMM" (day and abbreviated month)
            Text(
                text = dateStr,
                fontSize = 16.sp, // Slightly smaller to fit the additional text
                fontWeight = FontWeight.Bold,
                color = if (hasError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
        
        // Down arrow
        IconButton(
            onClick = {
                val newIndex = if (selectedIndex >= daysList.size - 1) daysList.size - 1 else selectedIndex + 1
                onIndexChange(newIndex)
            },
            modifier = Modifier.size(48.dp) // Larger touch target
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = "Select next day", // More descriptive
                tint = if (hasError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * A number picker component that allows scrolling through numbers
 * 
 * @param value The current value
 * @param onValueChange Callback when the value changes
 * @param range The range of allowed values
 * @param format Function to format the value for display
 * @param hasError Whether to show an error state
 */
@Composable
fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    format: (Int) -> String = { it.toString() },
    hasError: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .then(
                if (hasError) {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier
                }
            )
            .padding(vertical = 8.dp)
    ) {
        // Up arrow
        IconButton(
            onClick = {
                val newValue = if (value >= range.last) range.first else value + 1
                onValueChange(newValue)
            },
            modifier = Modifier.size(48.dp) // Larger touch target
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowDropUp,
                contentDescription = "Increase value", // More descriptive
                tint = if (hasError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
        
        // Current value
        Box(
            modifier = Modifier
                .height(40.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = format(value),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (hasError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }
        
        // Down arrow
        IconButton(
            onClick = {
                val newValue = if (value <= range.first) range.last else value - 1
                onValueChange(newValue)
            },
            modifier = Modifier.size(48.dp) // Larger touch target
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = "Decrease value", // More descriptive
                tint = if (hasError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
} 