package wesseling.io.fasttime.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import wesseling.io.fasttime.model.FastingState
import wesseling.io.fasttime.model.ThemePreference
import wesseling.io.fasttime.settings.PreferencesManager
import androidx.compose.ui.platform.LocalContext

// Light theme colors
private val NotFastingGrayLight = Color(0xFF757575)
private val EarlyFastingYellowLight = Color(0xFFF59E0B)
private val KetosisBlueLight = Color(0xFF3B82F6)
private val AutophagyGreenLight = Color(0xFF059669)
private val DeepFastingPurpleLight = Color(0xFF8B5CF6)

// Dark theme colors - slightly brighter for better visibility
private val NotFastingGrayDark = Color(0xFF9E9E9E)
private val EarlyFastingYellowDark = Color(0xFFFFB74D)
private val KetosisBlueDark = Color(0xFF64B5F6)
private val AutophagyGreenDark = Color(0xFF4CAF50)
private val DeepFastingPurpleDark = Color(0xFFB39DDB)

/**
 * Get the appropriate color for the current fasting state, adjusted for the current theme
 */
@Composable
fun getColorForFastingState(fastingState: FastingState): Color {
    val context = LocalContext.current
    val preferencesManager = PreferencesManager.getInstance(context)
    val themePreference = preferencesManager.dateTimePreferences.themePreference
    val systemDarkTheme = isSystemInDarkTheme()
    
    // Determine if dark theme is being used
    val useDarkTheme = when (themePreference) {
        ThemePreference.SYSTEM -> systemDarkTheme
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }
    
    return when (fastingState) {
        FastingState.NOT_FASTING -> if (useDarkTheme) NotFastingGrayDark else NotFastingGrayLight
        FastingState.EARLY_FAST -> if (useDarkTheme) EarlyFastingYellowDark else EarlyFastingYellowLight
        FastingState.KETOSIS -> KetosisBlueLight // Same for both themes as it's already visible
        FastingState.AUTOPHAGY -> if (useDarkTheme) AutophagyGreenDark else AutophagyGreenLight
        FastingState.DEEP_FASTING -> if (useDarkTheme) DeepFastingPurpleDark else DeepFastingPurpleLight
    }
} 