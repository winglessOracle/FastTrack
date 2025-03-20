package wesseling.io.fasttime.model

import android.content.Context
import wesseling.io.fasttime.R

/**
 * Represents different fasting states with their time thresholds and descriptions
 */
enum class FastingState(
    val nameResId: Int,
    val descriptionResId: Int,
    val hourThreshold: Int
) {
    NOT_FASTING(R.string.fasting_state_not_fasting_name, R.string.fasting_state_not_fasting_description, 0),
    EARLY_FAST(R.string.fasting_state_early_fast_name, R.string.fasting_state_early_fast_description, 4),
    GLYCOGEN_DEPLETION(R.string.fasting_state_glycogen_depletion_name, R.string.fasting_state_glycogen_depletion_description, 12),
    METABOLIC_SHIFT(R.string.fasting_state_metabolic_shift_name, R.string.fasting_state_metabolic_shift_description, 18),
    DEEP_KETOSIS(R.string.fasting_state_deep_ketosis_name, R.string.fasting_state_deep_ketosis_description, 24),
    IMMUNE_RESET(R.string.fasting_state_immune_reset_name, R.string.fasting_state_immune_reset_description, 48),
    EXTENDED_FAST(R.string.fasting_state_extended_fast_name, R.string.fasting_state_extended_fast_description, 72);

    /**
     * Get the localized display name for this fasting state
     */
    fun getDisplayName(context: Context): String {
        return context.getString(nameResId)
    }
    
    /**
     * Get the localized description for this fasting state
     */
    fun getDescription(context: Context): String {
        return context.getString(descriptionResId)
    }
    
    // For backward compatibility
    val displayName: String
        get() = when (this) {
            NOT_FASTING -> "Fed State"
            EARLY_FAST -> "Early Fasting"
            GLYCOGEN_DEPLETION -> "Glycogen Depletion" 
            METABOLIC_SHIFT -> "Metabolic Shift"
            DEEP_KETOSIS -> "Deep Ketosis"
            IMMUNE_RESET -> "Immune Reset"
            EXTENDED_FAST -> "Extended Fast"
        }
    
    // For backward compatibility
    val description: String
        get() = when (this) {
            NOT_FASTING -> "Digestion & Absorption"
            EARLY_FAST -> "Fat Burning Begins"
            GLYCOGEN_DEPLETION -> "Fat Metabolism Increases" 
            METABOLIC_SHIFT -> "Ketosis Begins"
            DEEP_KETOSIS -> "Autophagy Peaks"
            IMMUNE_RESET -> "Stem Cell Production"
            EXTENDED_FAST -> "Cellular Rejuvenation"
        }

    companion object {
        /**
         * Get the fasting state based on elapsed hours
         */
        fun getStateForHours(hours: Int): FastingState {
            return values().reversed().find { hours >= it.hourThreshold } ?: NOT_FASTING
        }
        
        /**
         * Get the fasting state based on elapsed milliseconds
         */
        fun getStateForDuration(durationMillis: Long): FastingState {
            val hours = (durationMillis / (1000 * 60 * 60)).toInt()
            return getStateForHours(hours)
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