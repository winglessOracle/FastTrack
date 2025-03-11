package wesseling.io.fasttime.model

/**
 * Represents different fasting states with their time thresholds and descriptions
 */
enum class FastingState(
    val displayName: String,
    val description: String,
    val hourThreshold: Int
) {
    NOT_FASTING("Fed", "Not fasting", 0),
    EARLY_FAST("Early Fast", "Fat burning begins", 5),
    KETOSIS("Ketosis", "Fat burning accelerates", 13),
    AUTOPHAGY("Autophagy", "Cell repair begins", 17),
    DEEP_FASTING("Deep Fasting", "Growth hormone increases", 25);

    companion object {
        /**
         * Get the fasting state based on elapsed hours
         */
        fun getStateForHours(hours: Int): FastingState {
            return values().reversed().find { hours >= it.hourThreshold } ?: NOT_FASTING
        }
        
        /**
         * Get the next fasting state based on the current state
         */
        fun FastingState.getNextState(): FastingState? {
            val currentIndex = ordinal
            return if (currentIndex < values().size - 1) {
                values()[currentIndex + 1]
            } else {
                null
            }
        }
        
        /**
         * Get hours until the next fasting state
         */
        fun getHoursUntilNextState(currentHours: Int): Int? {
            val currentState = getStateForHours(currentHours)
            val nextState = currentState.getNextState() ?: return null
            return nextState.hourThreshold - currentHours
        }
    }
} 