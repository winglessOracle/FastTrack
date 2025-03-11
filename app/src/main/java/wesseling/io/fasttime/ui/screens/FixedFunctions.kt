package wesseling.io.fasttime.ui.screens

import wesseling.io.fasttime.model.CompletedFast

/**
 * Calculate the longest fast duration in hours
 */
fun calculateLongestFast(fasts: List<CompletedFast>): Double {
    if (fasts.isEmpty()) return 0.0
    return (fasts.maxOfOrNull { it.durationMillis } ?: 0L) / (1000.0 * 60 * 60)
} 