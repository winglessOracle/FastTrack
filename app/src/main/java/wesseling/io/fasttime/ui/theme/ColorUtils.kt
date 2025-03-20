package wesseling.io.fasttime.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import wesseling.io.fasttime.model.FastingState
import wesseling.io.fasttime.model.ThemePreference
import wesseling.io.fasttime.settings.PreferencesManager
import androidx.compose.ui.platform.LocalContext

// Dark theme colors - slightly brighter for better visibility
private val NotFastingGrayDark = Color(0xFF9E9E9E)
private val EarlyFastingYellowDark = Color(0xFFFFB74D)
private val GlycogenDepletionOrangeDark = Color(0xFFFF9800)
private val MetabolicShiftBlueDark = Color(0xFF64B5F6)
private val DeepKetosisGreenDark = Color(0xFF4CAF50)
private val ImmuneResetPurpleDark = Color(0xFFB39DDB)
private val ExtendedFastMagentaDark = Color(0xFFEC4899)

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
        FastingState.NOT_FASTING -> if (useDarkTheme) NotFastingGrayDark else NotFastingGray
        FastingState.EARLY_FAST -> if (useDarkTheme) EarlyFastingYellowDark else EarlyFastingYellow
        FastingState.GLYCOGEN_DEPLETION -> if (useDarkTheme) GlycogenDepletionOrangeDark else GlycogenDepletionOrange
        FastingState.METABOLIC_SHIFT -> if (useDarkTheme) MetabolicShiftBlueDark else MetabolicShiftBlue
        FastingState.DEEP_KETOSIS -> if (useDarkTheme) DeepKetosisGreenDark else DeepKetosisGreen
        FastingState.IMMUNE_RESET -> if (useDarkTheme) ImmuneResetPurpleDark else ImmuneResetPurple
        FastingState.EXTENDED_FAST -> if (useDarkTheme) ExtendedFastMagentaDark else ExtendedFastMagenta
    }
} 