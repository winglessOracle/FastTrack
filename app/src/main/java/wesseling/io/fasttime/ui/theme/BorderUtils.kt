package wesseling.io.fasttime.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Constants and utilities for borders used throughout the app
 */
object BorderUtils {
    // Color used for the active timer border (same as the widget border)
    val ACTIVE_TIMER_BORDER_COLOR = Color(0xFF4CAF50)  // Green color
    
    // Border width used for active timer indicators
    val ACTIVE_TIMER_BORDER_WIDTH = 4.dp
    
    /**
     * Creates a standard border for active timers
     */
    fun createActiveTimerBorder(): BorderStroke {
        return BorderStroke(
            width = ACTIVE_TIMER_BORDER_WIDTH,
            color = ACTIVE_TIMER_BORDER_COLOR
        )
    }
} 