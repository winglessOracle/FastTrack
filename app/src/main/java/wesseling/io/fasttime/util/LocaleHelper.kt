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
            ENGLISH("en", "English");
            
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
            val localeList = LocaleListCompat.create(locale)
            AppCompatDelegate.setApplicationLocales(localeList)
            
            // The above is the preferred method, but for older support, also
            // update the configuration directly
            Locale.setDefault(locale)
            
            val resources = context.resources
            val configuration = Configuration(resources.configuration)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                configuration.setLocale(locale)
            } else {
                @Suppress("DEPRECATION")
                configuration.locale = locale
            }
            
            return context.createConfigurationContext(configuration)
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