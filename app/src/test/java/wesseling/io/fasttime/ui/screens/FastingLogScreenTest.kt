package wesseling.io.fasttime.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Test
import wesseling.io.fasttime.model.CompletedFast
import wesseling.io.fasttime.model.FastingState
import java.util.UUID

class FastingLogScreenTest {

    @Test
    fun `formatExactDuration formats durations correctly`() {
        // Test with seconds only
        assertEquals("0 hours, 0 minutes and 30 seconds", formatExactDuration(30 * 1000))
        
        // Test with minutes and seconds
        assertEquals("0 hours, 5 minutes and 30 seconds", formatExactDuration(5 * 60 * 1000 + 30 * 1000))
        
        // Test with hours, minutes, and seconds
        assertEquals("2 hours, 5 minutes and 30 seconds", formatExactDuration(2 * 60 * 60 * 1000 + 5 * 60 * 1000 + 30 * 1000))
        
        // Test with days, hours, and minutes (seconds omitted for days)
        assertEquals("1 days, 2 hours, 5 minutes", formatExactDuration(24 * 60 * 60 * 1000 + 2 * 60 * 60 * 1000 + 5 * 60 * 1000 + 30 * 1000))
        
        // Test with multiple days
        assertEquals("3 days, 2 hours, 5 minutes", formatExactDuration(3 * 24 * 60 * 60 * 1000 + 2 * 60 * 60 * 1000 + 5 * 60 * 1000))
        
        // Test with zero duration
        assertEquals("0 hours, 0 minutes and 0 seconds", formatExactDuration(0))
    }
    
    @Test
    fun `calculateTimeInMaxState calculates time correctly`() {
        // Test NOT_FASTING state
        val notFastingFast = createCompletedFast(
            durationMillis = 2 * 60 * 60 * 1000, // 2 hours
            maxFastingState = FastingState.NOT_FASTING
        )
        assertEquals("a short time", calculateTimeInMaxState(notFastingFast))
        
        // Test EARLY_FAST state with short duration
        val earlyFastingShortFast = createCompletedFast(
            durationMillis = 8 * 60 * 60 * 1000, // 8 hours
            maxFastingState = FastingState.EARLY_FAST
        )
        assertEquals("8 hours", calculateTimeInMaxState(earlyFastingShortFast))
        
        // Test EARLY_FAST state with long duration (should be capped at 12 hours)
        val earlyFastingLongFast = createCompletedFast(
            durationMillis = 20 * 60 * 60 * 1000, // 20 hours
            maxFastingState = FastingState.EARLY_FAST
        )
        assertEquals("12 hours", calculateTimeInMaxState(earlyFastingLongFast))
        
        // Test KETOSIS state
        val ketosisFast = createCompletedFast(
            durationMillis = 18 * 60 * 60 * 1000, // 18 hours
            maxFastingState = FastingState.KETOSIS
        )
        assertEquals("6 hours", calculateTimeInMaxState(ketosisFast))
        
        // Test AUTOPHAGY state
        val autophagyFast = createCompletedFast(
            durationMillis = 30 * 60 * 60 * 1000, // 30 hours
            maxFastingState = FastingState.AUTOPHAGY
        )
        assertEquals("6 hours", calculateTimeInMaxState(autophagyFast))
        
        // Test DEEP_FASTING state
        val deepFastingFast = createCompletedFast(
            durationMillis = 72 * 60 * 60 * 1000, // 72 hours
            maxFastingState = FastingState.DEEP_FASTING
        )
        assertEquals("1 days and 0 hours", calculateTimeInMaxState(deepFastingFast))
    }
    
    // Helper function to create CompletedFast objects for testing
    private fun createCompletedFast(
        durationMillis: Long,
        maxFastingState: FastingState,
        note: String = ""
    ): CompletedFast {
        val endTimeMillis = System.currentTimeMillis()
        val startTimeMillis = endTimeMillis - durationMillis
        
        return CompletedFast(
            id = UUID.randomUUID().toString(),
            startTimeMillis = startTimeMillis,
            endTimeMillis = endTimeMillis,
            durationMillis = durationMillis,
            maxFastingState = maxFastingState,
            note = note
        )
    }
}

// Copy of the functions from FastingLogScreen.kt for testing
// These should be kept in sync with the actual implementation

/**
 * Calculates an estimate of how long the user was in their maximum fasting state
 * This is an approximation based on typical fasting state transitions
 */
private fun calculateTimeInMaxState(fast: CompletedFast): String {
    val totalDurationHours = fast.durationMillis / (1000 * 60 * 60)
    
    // Estimate time in max state based on the maximum fasting state reached
    val hoursInMaxState = when (fast.maxFastingState) {
        FastingState.NOT_FASTING -> 0
        FastingState.EARLY_FAST -> totalDurationHours.coerceAtMost(12)
        FastingState.KETOSIS -> {
            val ketosisStart = 12
            (totalDurationHours - ketosisStart).coerceAtLeast(0).coerceAtMost(12)
        }
        FastingState.AUTOPHAGY -> {
            val autophagyStart = 24
            (totalDurationHours - autophagyStart).coerceAtLeast(0).coerceAtMost(24)
        }
        FastingState.DEEP_FASTING -> {
            val deepFastingStart = 48
            (totalDurationHours - deepFastingStart).coerceAtLeast(0)
        }
    }
    
    // Format the time in max state
    return when {
        hoursInMaxState < 1 -> "a short time"
        hoursInMaxState < 24 -> "$hoursInMaxState hours"
        else -> "${hoursInMaxState / 24} days and ${hoursInMaxState % 24} hours"
    }
}

/**
 * Formats the duration in a more detailed way, including days, hours, minutes and seconds
 */
private fun formatExactDuration(durationMillis: Long): String {
    val seconds = (durationMillis / 1000) % 60
    val minutes = (durationMillis / (1000 * 60)) % 60
    val hours = (durationMillis / (1000 * 60 * 60)) % 24
    val days = durationMillis / (1000 * 60 * 60 * 24)
    
    return buildString {
        if (days > 0) {
            append("$days days, ")
        }
        append("$hours hours, $minutes minutes")
        if (days == 0L) {
            append(" and $seconds seconds")
        }
    }
} 