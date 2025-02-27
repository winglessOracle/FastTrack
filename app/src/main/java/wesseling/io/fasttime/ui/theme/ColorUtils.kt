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
private val NotFastingRedLight = Color(0xFFEF4444)
private val EarlyFastingYellowLight = Color(0xFFF59E0B)
private val KetosisBlueLight = Color(0xFF3B82F6)
private val AutophagyGreenLight = Color(0xFF059669)
private val DeepFastingPurpleLight = Color(0xFF8B5CF6)

// Dark theme colors - slightly brighter for better visibility
private val NotFastingRedDark = Color(0xFFFF6B6B)
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
        FastingState.NOT_FASTING -> if (useDarkTheme) NotFastingRedDark else NotFastingRedLight
        FastingState.EARLY_FAST -> if (useDarkTheme) EarlyFastingYellowDark else EarlyFastingYellowLight
        FastingState.KETOSIS -> if (useDarkTheme) KetosisBlueDark else KetosisBlueLight
        FastingState.AUTOPHAGY -> if (useDarkTheme) AutophagyGreenDark else AutophagyGreenLight
        FastingState.DEEP_FASTING -> if (useDarkTheme) DeepFastingPurpleDark else DeepFastingPurpleLight
    }
} 