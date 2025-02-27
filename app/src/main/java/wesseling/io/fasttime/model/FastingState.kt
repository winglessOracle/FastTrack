package wesseling.io.fasttime.model

/**
 * Enum representing different fasting states based on elapsed time
 */
enum class FastingState(
    val displayName: String,
    val description: String,
    val hourThreshold: Int
) {
    NOT_FASTING("Fed", "Eating period", 0),
    EARLY_FAST("Early Fast", "Fat burning begins", 4),
    KETOSIS("Ketosis", "Fat burning accelerates", 12),
    AUTOPHAGY("Autophagy", "Cell repair begins", 16),
    DEEP_FASTING("Deep Fasting", "Growth hormone increases", 24);
    
    companion object {
        /**
         * Get the fasting state based on elapsed hours
         */
        fun getStateForHours(hours: Int): FastingState {
            return when {
                hours >= DEEP_FASTING.hourThreshold -> DEEP_FASTING
                hours >= AUTOPHAGY.hourThreshold -> AUTOPHAGY
                hours >= KETOSIS.hourThreshold -> KETOSIS
                hours >= EARLY_FAST.hourThreshold -> EARLY_FAST
                else -> NOT_FASTING
            }
        }
        
        /**
         * Get the next fasting state
         */
        fun FastingState.getNextState(): FastingState? {
            return when (this) {
                NOT_FASTING -> EARLY_FAST
                EARLY_FAST -> KETOSIS
                KETOSIS -> AUTOPHAGY
                AUTOPHAGY -> DEEP_FASTING
                DEEP_FASTING -> null
            }
        }
        
        /**
         * Get hours until the next fasting state
         */
        fun FastingState.getHoursUntilNextState(currentHours: Int): Int? {
            val nextState = getNextState() ?: return null
            return nextState.hourThreshold - currentHours
        }
    }
} 