package wesseling.io.fasttime.model

/**
 * Represents different fasting states with their time thresholds and descriptions
 */
enum class FastingState(
    val displayName: String,
    val description: String,
    val hourThreshold: Int
) {
    NOT_FASTING("Fed State", "Digestion & Absorption", 0),
    EARLY_FAST("Early Fasting", "Fat Burning Begins", 4),
    GLYCOGEN_DEPLETION("Glycogen Depletion", "Fat Metabolism Increases", 12),
    METABOLIC_SHIFT("Metabolic Shift", "Ketosis Begins", 18),
    DEEP_KETOSIS("Deep Ketosis", "Autophagy Peaks", 24),
    IMMUNE_RESET("Immune Reset", "Stem Cell Production", 48),
    EXTENDED_FAST("Extended Fast", "Cellular Rejuvenation", 72);

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