package wesseling.io.fasttime

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Process
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import wesseling.io.fasttime.timer.FastingTimer
import wesseling.io.fasttime.util.BitmapPool
import wesseling.io.fasttime.util.LocaleHelper
import wesseling.io.fasttime.widget.WidgetBackgroundHelper

/**
 * Custom Application class for FastTrack app
 * Handles global initialization and cleanup of singletons
 */
class FastTrackApplication : Application(), LifecycleEventObserver {
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application onCreate")
        
        // Apply saved language settings
        applyLanguageSettings()
        
        // Register for process lifecycle events
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        
        // Initialize singletons with application context
        initializeSingletons(applicationContext)
    }
    
    /**
     * Apply saved language settings
     */
    private fun applyLanguageSettings() {
        val languageCode = LocaleHelper.getLanguageCode(this)
        Log.d(TAG, "Applying saved language: $languageCode")
        
        // First update the locale using LocaleHelper
        LocaleHelper.updateLocale(this, languageCode)
        
        // Also set the default locale for the JVM - this helps with formatting
        if (languageCode.isNotEmpty()) {
            val locale = java.util.Locale(languageCode)
            java.util.Locale.setDefault(locale)
            
            // Create an ordered locale list with the selected language first, followed by English as fallback
            val locales = arrayOfNulls<java.util.Locale>(2)
            locales[0] = locale
            locales[1] = java.util.Locale.ENGLISH  // Always use English as the fallback, not German
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                java.util.Locale.setDefault(java.util.Locale.Category.DISPLAY, locale)
                java.util.Locale.setDefault(java.util.Locale.Category.FORMAT, locale)
            }
            
            Log.d(TAG, "Default locale set to: $locale with English fallback")
        } else {
            Log.d(TAG, "Using system default locale")
        }
    }
    
    /**
     * Called when configuration changes, including locale changes
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d(TAG, "Configuration changed")
        applyLanguageSettings()
    }
    
    /**
     * Called when a Context is attached to this Application
     * Ensures all new contexts have the correct locale
     */
    override fun attachBaseContext(base: Context) {
        // Apply the saved language settings to the base context
        val languageCode = LocaleHelper.getLanguageCode(base)
        val updatedContext = LocaleHelper.updateLocale(base, languageCode)
        super.attachBaseContext(updatedContext)
    }
    
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        Log.d(TAG, "Lifecycle event: $event")
        when (event) {
            Lifecycle.Event.ON_START -> {
                // App moved to foreground
                Log.d(TAG, "App moved to foreground")
                
                // Check if widget service should be running and ensure it is
                checkAndRestoreWidgetService()
            }
            Lifecycle.Event.ON_STOP -> {
                // App moved to background
                Log.d(TAG, "App moved to background, saving state")
                saveAllState()
                
                // Clear memory caches to free up memory
                clearMemoryCaches()
                
                // Clean up cache files when app goes to background
                performCacheCleanup()
            }
            Lifecycle.Event.ON_DESTROY -> {
                // App is being destroyed
                Log.d(TAG, "App is being destroyed, cleaning up")
                cleanupSingletons()
            }
            else -> { /* ignore other events */ }
        }
    }
    
    override fun onTerminate() {
        super.onTerminate()
        Log.d(TAG, "Application onTerminate")
        
        // Clean up singletons
        cleanupSingletons()
        
        // Remove lifecycle observer
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        Log.d(TAG, "Application onLowMemory")
        
        // Save state and clean up non-essential resources
        saveAllState()
        
        // Clear all memory caches
        clearMemoryCaches()
        
        // Clean up cache files when memory is low
        performCacheCleanup()
    }
    
    /**
     * Initialize all singleton instances with application context
     */
    private fun initializeSingletons(context: Context) {
        try {
            // Initialize FastingTimer with application context
            FastingTimer.getInstance(context)
            Log.d(TAG, "Singletons initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing singletons", e)
        }
    }
    
    /**
     * Save state for all singletons
     */
    private fun saveAllState() {
        try {
            // Nothing to do here as FastingTimer saves state automatically
            Log.d(TAG, "All state saved successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving state", e)
        }
    }
    
    /**
     * Clear memory caches to free up memory
     */
    private fun clearMemoryCaches() {
        try {
            // Clear widget background memory cache
            WidgetBackgroundHelper.clearMemoryCache()
            
            // Clear bitmap pool
            BitmapPool.getInstance().clear()
            
            // Run garbage collection to reclaim memory
            System.gc()
            
            Log.d(TAG, "Memory caches cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing memory caches", e)
        }
    }
    
    /**
     * Perform cleanup of cache files
     * This is a good time to clean up as the app is not actively being used
     */
    private fun performCacheCleanup() {
        try {
            // Clean up widget background cache files
            WidgetBackgroundHelper.cleanupCacheFiles(applicationContext)
            
            // Update the last cleanup time in preferences
            val prefs = applicationContext.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
            prefs.edit().putLong("last_cache_cleanup", System.currentTimeMillis()).apply()
            
            Log.d(TAG, "Cache files cleanup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up cache files", e)
        }
    }
    
    /**
     * Clean up singleton instances
     */
    private fun cleanupSingletons() {
        try {
            // Destroy FastingTimer instance
            FastingTimer.destroyInstance()
            
            // Clear memory caches
            clearMemoryCaches()
            
            Log.d(TAG, "Singletons cleaned up successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up singletons", e)
        }
    }
    
    /**
     * Check if the widget service should be running and restore it if needed
     * This ensures widgets continue to update even if the service was killed
     */
    private fun checkAndRestoreWidgetService() {
        try {
            val fastingTimer = FastingTimer.getInstance(applicationContext)
            
            // Only start service if timer is running
            if (fastingTimer.isRunning) {
                Log.d(TAG, "Timer is running, ensuring widget update service is running")
                
                // Use service's recovery mechanism
                val serviceClass = Class.forName("wesseling.io.fasttime.widget.FastingWidgetUpdateService")
                val ensureRunningMethod = serviceClass.getMethod("ensureServiceRunning", Context::class.java)
                ensureRunningMethod.invoke(null, applicationContext)
                
                Log.d(TAG, "Widget service recovery check complete")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking/restoring widget service", e)
            
            // Fallback to direct recovery if reflection fails
            try {
                val fastingTimer = FastingTimer.getInstance(applicationContext)
                
                if (fastingTimer.isRunning) {
                    // Try to restart widget update service directly
                    val serviceIntent = Intent()
                    serviceIntent.setClassName(
                        "wesseling.io.fasttime.widget",
                        "wesseling.io.fasttime.widget.FastingWidgetUpdateService"
                    )
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        applicationContext.startForegroundService(serviceIntent)
                    } else {
                        applicationContext.startService(serviceIntent)
                    }
                    
                    Log.d(TAG, "Direct widget service restart attempted")
                }
            } catch (e2: Exception) {
                Log.e(TAG, "Fallback widget service recovery also failed", e2)
            }
        }
    }
    
    companion object {
        private const val TAG = "FastTrackApplication"
    }
} 