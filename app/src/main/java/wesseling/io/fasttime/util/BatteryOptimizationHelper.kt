package wesseling.io.fasttime.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.util.Log

/**
 * Helper class for managing battery optimization settings
 * 
 * This utility class provides methods to check and manage the app's battery optimization status.
 * Android's battery optimization can restrict background operations, which may affect the app's
 * ability to update widgets and send notifications reliably. This class helps:
 * 
 * 1. Check if the app is exempt from battery optimizations
 * 2. Create intents to request exemption from battery optimizations
 * 3. Navigate users to the system battery optimization settings
 * 
 * The implementation includes error handling to ensure the app
 * functions correctly across different Android versions.
 */
object BatteryOptimizationHelper {
    private const val TAG = "BatteryOptimizationHelper"
    
    /**
     * Check if the app is exempt from battery optimizations
     * 
     * This method determines whether the app has been granted permission to ignore battery
     * optimizations by the system. When an app is exempt from battery optimizations, it can
     * perform background operations more reliably, which is essential for widget updates
     * and scheduled notifications.
     * 
     * @param context The application context
     * @return true if the app is exempt from battery optimizations, false otherwise
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        return try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking battery optimization status", e)
            false
        }
    }
    
    /**
     * Create an intent to request battery optimization exemption
     * 
     * This method creates an intent that, when launched, will prompt the user to exempt
     * the app from battery optimizations. This is useful for ensuring reliable background
     * operations, especially for widget updates and notifications.
     * 
     * @param context The application context
     * @return An Intent to request battery optimization exemption, or null if an error occurs
     */
    fun createBatteryOptimizationIntent(context: Context): Intent? {
        return try {
            Intent().apply {
                action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                data = Uri.parse("package:${context.packageName}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating battery optimization intent", e)
            null
        }
    }
    
    /**
     * Create an intent to open battery optimization settings
     * 
     * This method creates an intent that, when launched, will open the system's battery
     * optimization settings screen. This allows users to manually adjust battery optimization
     * settings for the app if the direct request approach is not suitable or fails.
     * 
     * @return An Intent to open the battery optimization settings screen
     */
    fun createBatteryOptimizationSettingsIntent(): Intent {
        return Intent().apply {
            action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
        }
    }
} 