package wesseling.io.fasttime.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import wesseling.io.fasttime.settings.PreferencesManager
import wesseling.io.fasttime.util.DateTimeFormatter

/**
 * A day picker component that allows scrolling through days
 */
@Composable
fun DayPicker(
    selectedIndex: Int,
    onIndexChange: (Int) -> Unit,
    daysList: List<Triple<Int, Int, Int>>
) {
    // Get preferences manager
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager.getInstance(context) }
    val preferences = remember { preferencesManager.dateTimePreferences }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(90.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
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
                tint = MaterialTheme.colorScheme.primary
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
            
            // Create a calendar with the selected date
            val cal = Calendar.getInstance().apply {
                set(year, month, day)
            }
            
            // Format the date using user preferences
            val dateStr = DateTimeFormatter.formatDate(cal.timeInMillis, preferences)
            
            Text(
                text = dateStr,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
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
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * A number picker component that allows scrolling through numbers
 */
@Composable
fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    format: (Int) -> String = { it.toString() }
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
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
                tint = MaterialTheme.colorScheme.primary
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
                color = MaterialTheme.colorScheme.onSurface
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
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
} 