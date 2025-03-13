package wesseling.io.fasttime

import android.app.Application
import android.content.Context
import android.os.Process
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import wesseling.io.fasttime.timer.FastingTimer
import wesseling.io.fasttime.widget.WidgetBackgroundHelper

/**
 * Custom Application class for FastTrack app
 * Handles global initialization and cleanup of singletons
 */
class FastTrackApplication : Application(), LifecycleEventObserver {
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application onCreate")
        
        // Register for process lifecycle events
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        
        // Initialize singletons with application context
        initializeSingletons(applicationContext)
    }
    
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        Log.d(TAG, "Lifecycle event: $event")
        when (event) {
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
    
    companion object {
        private const val TAG = "FastTrackApplication"
    }
} 