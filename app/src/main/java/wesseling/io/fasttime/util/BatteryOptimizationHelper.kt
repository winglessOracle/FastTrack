package wesseling.io.fasttime.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log

/**
 * Helper class for managing battery optimization settings
 */
object BatteryOptimizationHelper {
    private const val TAG = "BatteryOptimizationHelper"
    
    /**
     * Check if the app is exempt from battery optimizations
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        return try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                powerManager.isIgnoringBatteryOptimizations(context.packageName)
            } else {
                true // On older Android versions, we can't check or request this
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking battery optimization status", e)
            false
        }
    }
    
    /**
     * Create an intent to request battery optimization exemption
     */
    fun createBatteryOptimizationIntent(context: Context): Intent? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent().apply {
                    action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    data = Uri.parse("package:${context.packageName}")
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating battery optimization intent", e)
            null
        }
    }
    
    /**
     * Create an intent to open battery optimization settings
     */
    fun createBatteryOptimizationSettingsIntent(): Intent {
        return Intent().apply {
            action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
        }
    }
} 