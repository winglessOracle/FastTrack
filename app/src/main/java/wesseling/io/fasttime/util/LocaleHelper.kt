package wesseling.io.fasttime.util

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * Helper class for handling locale and language settings in the app
 */
class LocaleHelper {
    companion object {
        private const val PREF_LANGUAGE = "pref_language"
        
        /**
         * Supported languages in the app
         */
        enum class AppLanguage(val code: String, val displayName: String) {
            SYSTEM("", "System Default"),
            ENGLISH("en", "English"),
            SPANISH("es", "Español"),
            GERMAN("de", "Deutsch"),
            FRENCH("fr", "Français"),
            ITALIAN("it", "Italiano");
            
            companion object {
                fun fromCode(code: String): AppLanguage {
                    return values().find { it.code == code } ?: SYSTEM
                }
            }
        }
        
        /**
         * Update the app's locale based on the language code
         * 
         * @param context The application context
         * @param languageCode The language code to set, empty string means system default
         * @return An updated context with the new locale configuration
         */
        fun updateLocale(context: Context, languageCode: String): Context {
            // If using system default (empty language code), let the system handle it
            if (languageCode.isEmpty()) {
                return updateToSystemLocale(context)
            }
            
            // Otherwise, set the app-specific locale
            return updateToSpecificLocale(context, Locale(languageCode))
        }
        
        /**
         * Set the locale to the system default
         */
        private fun updateToSystemLocale(context: Context): Context {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
            
            // The configuration will be updated automatically on Android 7.0+
            return context
        }
        
        /**
         * Set the locale to a specific language
         */
        private fun updateToSpecificLocale(context: Context, locale: Locale): Context {
            // Use AppCompat to handle locale changes
            // Create a locale list with the primary locale first, then English as fallback
            val localeArray = arrayOf(locale, Locale.ENGLISH)
            val localeList = LocaleListCompat.create(*localeArray)
            AppCompatDelegate.setApplicationLocales(localeList)
            
            // Set the default locale for the JVM (for formatting, etc.)
            Locale.setDefault(locale)
            
            // For newer devices, set display and format locales separately
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Locale.setDefault(Locale.Category.DISPLAY, locale)
                Locale.setDefault(Locale.Category.FORMAT, locale)
            }
            
            // For older devices, update the configuration directly
            val res = context.resources
            val config = Configuration(res.configuration)
            
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                    // Android N and above supports locale lists
                    val systemLocaleList = android.os.LocaleList(*localeArray)
                    config.setLocales(systemLocaleList)
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 -> {
                    // Android 4.2+ supports setLocale
                    config.setLocale(locale)
                }
                else -> {
                    // Legacy support for older versions
                    @Suppress("DEPRECATION")
                    config.locale = locale
                }
            }
            
            // Create a new context with the updated configuration
            return context.createConfigurationContext(config)
        }
        
        /**
         * Get the currently selected language code from preferences
         */
        fun getLanguageCode(context: Context): String {
            val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            return prefs.getString(PREF_LANGUAGE, "") ?: ""
        }
        
        /**
         * Save a language code to preferences
         */
        fun setLanguageCode(context: Context, languageCode: String) {
            val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            prefs.edit().putString(PREF_LANGUAGE, languageCode).apply()
        }
        
        /**
         * Get the current app language
         */
        fun getCurrentLanguage(context: Context): AppLanguage {
            val code = getLanguageCode(context)
            return AppLanguage.fromCode(code)
        }
    }
} 