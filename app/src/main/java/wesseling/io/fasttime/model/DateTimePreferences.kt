package wesseling.io.fasttime.model

/**
 * Represents user preferences for date and time formats
 */
enum class DateFormat(val pattern: String, val displayName: String) {
    MDY_SLASH("MM/dd/yyyy", "MM/DD/YYYY (US)"),
    DMY_SLASH("dd/MM/yyyy", "DD/MM/YYYY (UK/EU)"),
    YMD_DASH("yyyy-MM-dd", "YYYY-MM-DD (ISO)"),
    MDY_TEXT("MMM d, yyyy", "Month D, YYYY")
}

/**
 * Represents time format preferences
 */
enum class TimeFormat(val pattern: String, val displayName: String) {
    HOURS_24("HH:mm", "24-hour (13:30)"),
    HOURS_12("h:mm a", "12-hour (1:30 PM)")
}

/**
 * Represents theme preferences
 */
enum class ThemePreference(val displayName: String) {
    SYSTEM("System default"),
    LIGHT("Light"),
    DARK("Dark")
}

/**
 * Represents widget update frequency preferences
 * Each option includes a multiplier that will be applied to the base update intervals
 * to adjust the frequency while maintaining the adaptive behavior.
 */
enum class UpdateFrequency(val displayName: String, val multiplier: Float) {
    VERY_FREQUENT("Very Frequent (Higher Battery Usage)", 0.5f),
    BALANCED("Balanced (Recommended)", 1.0f),
    BATTERY_SAVING("Battery Saving", 2.0f),
    MINIMAL("Minimal (Maximum Battery Saving)", 3.0f)
}

/**
 * Data class to hold user preferences
 */
data class DateTimePreferences(
    val dateFormat: DateFormat = DateFormat.DMY_SLASH,
    val timeFormat: TimeFormat = TimeFormat.HOURS_24,
    val showSeconds: Boolean = false,
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val enableFastingStateNotifications: Boolean = false,
    val updateFrequency: UpdateFrequency = UpdateFrequency.BALANCED
) {
    /**
     * Get the date pattern based on current preferences
     */
    fun getDatePattern(): String = dateFormat.pattern
    
    /**
     * Get the time pattern based on current preferences
     */
    fun getTimePattern(): String {
        return if (showSeconds) {
            when (timeFormat) {
                TimeFormat.HOURS_24 -> "HH:mm:ss"
                TimeFormat.HOURS_12 -> "h:mm:ss a"
            }
        } else {
            timeFormat.pattern
        }
    }
    
    /**
     * Get the combined date and time pattern
     */
    fun getDateTimePattern(): String {
        return "${getDatePattern()} ${getTimePattern()}"
    }
}
