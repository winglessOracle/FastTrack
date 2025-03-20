package wesseling.io.fasttime.util

import android.content.Context
import android.util.Log
import wesseling.io.fasttime.R
import wesseling.io.fasttime.model.CompletedFast

/**
 * Utility class for validating fasting entries
 */
object FastingValidator {
    private const val TAG = "FastingValidator"
    
    /**
     * Check if a fasting entry overlaps with any existing entries.
     * 
     * @param context Context to get string resources
     * @param newFast The fast to check for overlaps
     * @param existingFasts List of existing fasting entries
     * @param skipFastId Optional ID of a fast to skip in the check (useful when updating an existing fast)
     * @return Result object containing validity status and error message if invalid
     */
    fun checkForOverlappingFasts(
        context: Context,
        newFast: CompletedFast,
        existingFasts: List<CompletedFast>,
        skipFastId: String? = null
    ): OverlapCheckResult {
        // If list is empty, there can't be an overlap
        if (existingFasts.isEmpty()) {
            return OverlapCheckResult(isValid = true)
        }
        
        val newStart = newFast.startTimeMillis
        val newEnd = newFast.endTimeMillis
        
        // Basic validation: ensure start time is before end time
        if (newStart >= newEnd) {
            return OverlapCheckResult(
                isValid = false,
                errorMessage = context.getString(R.string.error_start_before_end)
            )
        }
        
        // Future time validation
        val currentTime = System.currentTimeMillis()
        if (newEnd > currentTime) {
            return OverlapCheckResult(
                isValid = false,
                errorMessage = context.getString(R.string.error_future_end_time)
            )
        }
        
        // Check for overlap with each existing fast
        for (existingFast in existingFasts) {
            // Skip the current fast when updating
            if (skipFastId != null && existingFast.id == skipFastId) {
                continue
            }
            
            val existingStart = existingFast.startTimeMillis
            val existingEnd = existingFast.endTimeMillis
            
            // Check for overlap: 
            // If new fast starts before existing fast ends AND 
            // new fast ends after existing fast starts
            if (newStart < existingEnd && newEnd > existingStart) {
                val startTimeFormatted = formatDateForLog(existingStart)
                val endTimeFormatted = formatDateForLog(existingEnd)
                
                val overlapMessage = context.getString(
                    R.string.error_fast_overlap, 
                    startTimeFormatted, 
                    endTimeFormatted
                )
                
                Log.d(TAG, "Overlap detected: $overlapMessage")
                return OverlapCheckResult(
                    isValid = false,
                    errorMessage = overlapMessage,
                    overlappingFastId = existingFast.id
                )
            }
        }
        
        // No overlaps found
        return OverlapCheckResult(isValid = true)
    }
    
    /**
     * Format a timestamp for log messages
     */
    private fun formatDateForLog(timeMillis: Long): String {
        val date = java.util.Date(timeMillis)
        val format = java.text.SimpleDateFormat("MM/dd/yyyy HH:mm", java.util.Locale.getDefault())
        return format.format(date)
    }
    
    /**
     * Result of the overlap check
     */
    data class OverlapCheckResult(
        val isValid: Boolean,
        val errorMessage: String? = null,
        val overlappingFastId: String? = null
    )
} 