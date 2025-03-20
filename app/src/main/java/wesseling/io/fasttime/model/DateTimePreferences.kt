package wesseling.io.fasttime.model

import android.content.Context
import wesseling.io.fasttime.R

/**
 * Represents user preferences for date and time formats
 */
enum class DateFormat(val pattern: String, val displayNameResId: Int) {
    MDY_SLASH("MM/dd/yyyy", R.string.date_format_mdy_slash),
    DMY_SLASH("dd/MM/yyyy", R.string.date_format_dmy_slash),
    YMD_DASH("yyyy-MM-dd", R.string.date_format_ymd_dash),
    MDY_TEXT("MMM d, yyyy", R.string.date_format_mdy_text);
    
    /**
     * Get the localized display name for this date format
     */
    fun getDisplayName(context: Context): String {
        return context.getString(displayNameResId)
    }
    
    // For backward compatibility
    val displayName: String
        get() = when (this) {
            MDY_SLASH -> "MM/DD/YYYY (US)"
            DMY_SLASH -> "DD/MM/YYYY (UK/EU)"
            YMD_DASH -> "YYYY-MM-DD (ISO)"
            MDY_TEXT -> "Month D, YYYY"
        }
}

/**
 * Represents time format preferences
 */
enum class TimeFormat(val pattern: String, val displayNameResId: Int) {
    HOURS_24("HH:mm", R.string.time_format_24h),
    HOURS_12("h:mm a", R.string.time_format_12h);
    
    /**
     * Get the localized display name for this time format
     */
    fun getDisplayName(context: Context): String {
        return context.getString(displayNameResId)
    }
    
    // For backward compatibility
    val displayName: String
        get() = when (this) {
            HOURS_24 -> "24-hour (13:30)"
            HOURS_12 -> "12-hour (1:30 PM)"
        }
}

/**
 * Represents theme preferences
 */
enum class ThemePreference(val displayNameResId: Int) {
    SYSTEM(R.string.theme_system),
    LIGHT(R.string.theme_light),
    DARK(R.string.theme_dark);
    
    /**
     * Get the localized display name for this theme preference
     */
    fun getDisplayName(context: Context): String {
        return context.getString(displayNameResId)
    }
    
    // For backward compatibility
    val displayName: String
        get() = when (this) {
            SYSTEM -> "System default"
            LIGHT -> "Light"
            DARK -> "Dark"
        }
}

/**
 * Represents widget update frequency preferences
 * Each option includes a multiplier that will be applied to the base update intervals
 * to adjust the frequency while maintaining the adaptive behavior.
 */
enum class UpdateFrequency(val displayNameResId: Int, val multiplier: Float) {
    VERY_FREQUENT(R.string.update_frequency_very_frequent, 0.5f),
    BALANCED(R.string.update_frequency_balanced, 1.0f),
    BATTERY_SAVING(R.string.update_frequency_battery_saving, 2.0f),
    MINIMAL(R.string.update_frequency_minimal, 3.0f);
    
    /**
     * Get the localized display name for this update frequency
     */
    fun getDisplayName(context: Context): String {
        return context.getString(displayNameResId)
    }
    
    // For backward compatibility
    val displayName: String
        get() = when (this) {
            VERY_FREQUENT -> "Very Frequent (Higher Battery Usage)"
            BALANCED -> "Balanced (Recommended)"
            BATTERY_SAVING -> "Battery Saving"
            MINIMAL -> "Minimal (Maximum Battery Saving)"
        }
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
