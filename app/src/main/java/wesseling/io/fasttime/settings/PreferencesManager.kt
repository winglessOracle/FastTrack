package wesseling.io.fasttime.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import wesseling.io.fasttime.model.DateFormat
import wesseling.io.fasttime.model.DateTimePreferences
import wesseling.io.fasttime.model.ThemePreference
import wesseling.io.fasttime.model.TimeFormat

/**
 * Manager for handling user preferences
 */
class PreferencesManager(context: Context) {
    // Shared preferences for persistence
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    // Current preferences as observable state
    var dateTimePreferences by mutableStateOf(loadPreferences())
        private set
    
    // Preference change listener
    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
        // Reload preferences when any preference changes
        dateTimePreferences = loadPreferences()
    }
    
    init {
        // Register the preference change listener
        prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }
    
    /**
     * Load preferences from SharedPreferences
     */
    private fun loadPreferences(): DateTimePreferences {
        val dateFormatOrdinal = prefs.getInt(KEY_DATE_FORMAT, DateFormat.MDY_SLASH.ordinal)
        val timeFormatOrdinal = prefs.getInt(KEY_TIME_FORMAT, TimeFormat.HOURS_24.ordinal)
        val showSeconds = prefs.getBoolean(KEY_SHOW_SECONDS, false)
        val themePreferenceOrdinal = prefs.getInt(KEY_THEME, ThemePreference.SYSTEM.ordinal)
        val enableNotifications = prefs.getBoolean(KEY_ENABLE_NOTIFICATIONS, false)
        
        val dateFormat = DateFormat.values().getOrElse(dateFormatOrdinal) { DateFormat.MDY_SLASH }
        val timeFormat = TimeFormat.values().getOrElse(timeFormatOrdinal) { TimeFormat.HOURS_24 }
        val themePreference = ThemePreference.values().getOrElse(themePreferenceOrdinal) { ThemePreference.SYSTEM }
        
        return DateTimePreferences(
            dateFormat = dateFormat, 
            timeFormat = timeFormat, 
            showSeconds = showSeconds,
            themePreference = themePreference,
            enableFastingStateNotifications = enableNotifications
        )
    }
    
    /**
     * Save preferences to SharedPreferences
     */
    fun savePreferences(preferences: DateTimePreferences) {
        prefs.edit().apply {
            putInt(KEY_DATE_FORMAT, preferences.dateFormat.ordinal)
            putInt(KEY_TIME_FORMAT, preferences.timeFormat.ordinal)
            putBoolean(KEY_SHOW_SECONDS, preferences.showSeconds)
            putInt(KEY_THEME, preferences.themePreference.ordinal)
            putBoolean(KEY_ENABLE_NOTIFICATIONS, preferences.enableFastingStateNotifications)
            apply()
        }
    }
    
    /**
     * Update date format preference
     */
    fun updateDateFormat(dateFormat: DateFormat) {
        savePreferences(dateTimePreferences.copy(dateFormat = dateFormat))
    }
    
    /**
     * Update time format preference
     */
    fun updateTimeFormat(timeFormat: TimeFormat) {
        savePreferences(dateTimePreferences.copy(timeFormat = timeFormat))
    }
    
    /**
     * Toggle seconds display
     */
    fun toggleShowSeconds(showSeconds: Boolean) {
        savePreferences(dateTimePreferences.copy(showSeconds = showSeconds))
    }
    
    /**
     * Update theme preference
     */
    fun updateTheme(themePreference: ThemePreference) {
        savePreferences(dateTimePreferences.copy(themePreference = themePreference))
    }
    
    /**
     * Toggle fasting state notifications
     */
    fun toggleFastingStateNotifications(enable: Boolean) {
        savePreferences(dateTimePreferences.copy(enableFastingStateNotifications = enable))
    }
    
    companion object {
        // Shared preferences constants
        const val PREFS_NAME = "wesseling.io.fasttime.SettingsPrefs"
        const val KEY_DATE_FORMAT = "date_format"
        const val KEY_TIME_FORMAT = "time_format"
        const val KEY_SHOW_SECONDS = "show_seconds"
        const val KEY_THEME = "theme"
        const val KEY_ENABLE_NOTIFICATIONS = "enable_notifications"
        
        // Singleton instance
        @Volatile
        private var INSTANCE: PreferencesManager? = null
        
        fun getInstance(context: Context): PreferencesManager {
            return INSTANCE ?: synchronized(this) {
                val instance = PreferencesManager(context)
                INSTANCE = instance
                instance
            }
        }
    }
} 