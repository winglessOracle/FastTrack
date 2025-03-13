package wesseling.io.fasttime.widget

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.LruCache
import wesseling.io.fasttime.model.FastingState
import wesseling.io.fasttime.model.ThemePreference
import wesseling.io.fasttime.settings.PreferencesManager
import java.io.File
import java.io.FileOutputStream

/**
 * Helper class to create and manage widget background drawables
 */
object WidgetBackgroundHelper {
    private const val TAG = "WidgetBackgroundHelper"
    
    // Light theme colors - matching the main app's ColorUtils.kt
    private const val NOT_FASTING_GRAY_LIGHT = "#757575"
    private const val EARLY_FASTING_YELLOW_LIGHT = "#F59E0B"
    private const val GLYCOGEN_DEPLETION_ORANGE_LIGHT = "#EA580C"
    private const val METABOLIC_SHIFT_BLUE_LIGHT = "#3B82F6"
    private const val DEEP_KETOSIS_GREEN_LIGHT = "#059669"
    private const val IMMUNE_RESET_PURPLE_LIGHT = "#8B5CF6"
    private const val EXTENDED_FAST_MAGENTA_LIGHT = "#DB2777"
    
    // Dark theme colors - matching the main app's ColorUtils.kt
    private const val NOT_FASTING_GRAY_DARK = "#9E9E9E"
    private const val EARLY_FASTING_YELLOW_DARK = "#FFB74D"
    private const val GLYCOGEN_DEPLETION_ORANGE_DARK = "#FF9800"
    private const val METABOLIC_SHIFT_BLUE_DARK = "#64B5F6"
    private const val DEEP_KETOSIS_GREEN_DARK = "#4CAF50"
    private const val IMMUNE_RESET_PURPLE_DARK = "#B39DDB"
    private const val EXTENDED_FAST_MAGENTA_DARK = "#EC4899"
    
    // Memory cache for background drawables
    // Use 1/8th of available memory for this cache
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8
    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            // The cache size will be measured in kilobytes
            return bitmap.byteCount / 1024
        }
    }
    
    // Cache cleanup - call this when the app is in the background
    fun clearMemoryCache() {
        memoryCache.evictAll()
        Log.d(TAG, "Memory cache cleared")
    }
    
    /**
     * Get color for fasting state, considering the current theme
     */
    fun getColorForFastingState(state: FastingState, context: Context): Int {
        // Determine if dark theme is being used
        val useDarkTheme = shouldUseDarkTheme(context)
        
        return when (state) {
            FastingState.NOT_FASTING -> 
                Color.parseColor(if (useDarkTheme) NOT_FASTING_GRAY_DARK else NOT_FASTING_GRAY_LIGHT)
            FastingState.EARLY_FAST -> 
                Color.parseColor(if (useDarkTheme) EARLY_FASTING_YELLOW_DARK else EARLY_FASTING_YELLOW_LIGHT)
            FastingState.GLYCOGEN_DEPLETION -> 
                Color.parseColor(if (useDarkTheme) GLYCOGEN_DEPLETION_ORANGE_DARK else GLYCOGEN_DEPLETION_ORANGE_LIGHT)
            FastingState.METABOLIC_SHIFT -> 
                Color.parseColor(if (useDarkTheme) METABOLIC_SHIFT_BLUE_DARK else METABOLIC_SHIFT_BLUE_LIGHT)
            FastingState.DEEP_KETOSIS -> 
                Color.parseColor(if (useDarkTheme) DEEP_KETOSIS_GREEN_DARK else DEEP_KETOSIS_GREEN_LIGHT)
            FastingState.IMMUNE_RESET -> 
                Color.parseColor(if (useDarkTheme) IMMUNE_RESET_PURPLE_DARK else IMMUNE_RESET_PURPLE_LIGHT)
            FastingState.EXTENDED_FAST -> 
                Color.parseColor(if (useDarkTheme) EXTENDED_FAST_MAGENTA_DARK else EXTENDED_FAST_MAGENTA_LIGHT)
        }
    }
    
    /**
     * Determine if dark theme should be used based on system settings and user preferences
     */
    private fun shouldUseDarkTheme(context: Context): Boolean {
        val preferencesManager = PreferencesManager.getInstance(context)
        val themePreference = preferencesManager.dateTimePreferences.themePreference
        
        return when (themePreference) {
            ThemePreference.SYSTEM -> isSystemInDarkTheme(context)
            ThemePreference.LIGHT -> false
            ThemePreference.DARK -> true
        }
    }
    
    /**
     * Check if the system is using dark theme
     */
    private fun isSystemInDarkTheme(context: Context): Boolean {
        return (context.resources.configuration.uiMode and 
                Configuration.UI_MODE_NIGHT_MASK) == 
                Configuration.UI_MODE_NIGHT_YES
    }
    
    /**
     * Get a background drawable for the widget based on the fasting state
     * This method checks memory cache first, then file cache, then creates a new drawable
     */
    fun getBackgroundDrawable(
        context: Context,
        stateColor: Int,
        borderColor: Int = Color.TRANSPARENT,
        borderWidth: Float = 0f
    ): Drawable {
        // Create a unique key for this drawable configuration
        val cacheKey = "widget_bg_${stateColor}_${borderColor}_${borderWidth.toInt()}"
        
        try {
            // 1. Check memory cache first (fastest)
            val cachedBitmap = memoryCache.get(cacheKey)
            if (cachedBitmap != null) {
                Log.d(TAG, "Using memory-cached background: $cacheKey")
                return BitmapDrawable(context.resources, cachedBitmap)
            }
            
            // 2. Check file cache next
            val file = File(context.cacheDir, "$cacheKey.png")
            if (file.exists() && file.length() > 0) {
                try {
                    // Load bitmap from file
                    val bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
                    if (bitmap != null) {
                        // Add to memory cache for future use
                        memoryCache.put(cacheKey, bitmap)
                        Log.d(TAG, "Loaded background from file cache: $cacheKey")
                        return BitmapDrawable(context.resources, bitmap)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to load cached drawable from file, will recreate", e)
                    // Continue to create a new one if loading failed
                }
            }
            
            // 3. Create new drawable
            Log.d(TAG, "Creating new background drawable: $cacheKey")
            
            // Create the bitmap with gradient and border
            val width = 800 // Width of the drawable
            val height = 240 // Height of the drawable
            val cornerRadius = 24f // Corner radius in dp
            
            // Create gradient colors
            val baseColor = stateColor
            val darkerColor = darkenColor(baseColor, 0.3f)
            
            // Create the bitmap
            val bitmap = createRoundRectBitmap(
                width, height, cornerRadius,
                baseColor, darkerColor,
                borderColor, borderWidth
            )
            
            // Save to memory cache
            memoryCache.put(cacheKey, bitmap)
            
            // Save to file cache in the background
            Thread {
                try {
                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
                    }
                    Log.d(TAG, "Saved background to file cache: $cacheKey")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save background to file cache", e)
                }
            }.start()
            
            return BitmapDrawable(context.resources, bitmap)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating background drawable", e)
            
            // Return a simple colored drawable as fallback
            val fallbackBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(fallbackBitmap)
            canvas.drawColor(stateColor)
            return BitmapDrawable(context.resources, fallbackBitmap)
        }
    }
    
    /**
     * Clean up old cache files that haven't been accessed recently
     * Call this periodically to prevent cache from growing too large
     */
    fun cleanupCacheFiles(context: Context) {
        Thread {
            try {
                val cacheDir = context.cacheDir
                if (!cacheDir.exists()) {
                    return@Thread
                }
                
                val currentTime = System.currentTimeMillis()
                val maxAge = 7 * 24 * 60 * 60 * 1000L // 7 days in milliseconds
                
                cacheDir.listFiles()?.forEach { file ->
                    if (file.name.startsWith("widget_bg_") && file.name.endsWith(".png")) {
                        val fileAge = currentTime - file.lastModified()
                        if (fileAge > maxAge) {
                            file.delete()
                            Log.d(TAG, "Deleted old cache file: ${file.name}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cleaning up cache files", e)
            }
        }.start()
    }
    
    // For backward compatibility - will be removed in future versions
    @Deprecated("Use getBackgroundDrawable instead", ReplaceWith("getBackgroundDrawable(context, stateColor, borderColor, borderWidth)"))
    fun createBackgroundDrawable(
        context: Context,
        stateColor: Int,
        borderColor: Int,
        borderWidth: Float
    ): String? {
        // Create a drawable and return its path
        val drawable = getBackgroundDrawable(context, stateColor, borderColor, borderWidth)
        
        // For compatibility, save to file and return the path
        val cacheKey = "widget_bg_${stateColor}_${borderColor}_${borderWidth.toInt()}"
        val file = File(context.cacheDir, "$cacheKey.png")
        
        try {
            if (drawable is BitmapDrawable) {
                FileOutputStream(file).use { out ->
                    drawable.bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
                }
                return file.absolutePath
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in compatibility method", e)
        }
        
        return null
    }
    
    /**
     * Create a rounded rectangle bitmap with gradient and border
     */
    private fun createRoundRectBitmap(
        width: Int,
        height: Int,
        cornerRadius: Float,
        startColor: Int,
        endColor: Int,
        borderColor: Int = Color.TRANSPARENT,
        borderWidth: Float = 0f
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Draw the fill with gradient
        val fillPaint = Paint().apply {
            isAntiAlias = true
            shader = android.graphics.LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                startColor, endColor,
                android.graphics.Shader.TileMode.CLAMP
            )
            style = Paint.Style.FILL
        }
        
        val fillRect = RectF(
            borderWidth, 
            borderWidth, 
            width - borderWidth, 
            height - borderWidth
        )
        canvas.drawRoundRect(fillRect, cornerRadius - borderWidth, cornerRadius - borderWidth, fillPaint)
        
        // Draw the border
        if (borderWidth > 0 && borderColor != Color.TRANSPARENT) {
            val borderPaint = Paint().apply {
                isAntiAlias = true
                color = borderColor
                style = Paint.Style.STROKE
                strokeWidth = borderWidth
            }
            
            val borderRect = RectF(
                borderWidth / 2, 
                borderWidth / 2, 
                width - borderWidth / 2, 
                height - borderWidth / 2
            )
            canvas.drawRoundRect(borderRect, cornerRadius, cornerRadius, borderPaint)
        }
        
        return bitmap
    }
    
    /**
     * Darken a color by a given factor
     */
    private fun darkenColor(color: Int, factor: Float): Int {
        val a = Color.alpha(color)
        val r = Math.max(Color.red(color) * (1 - factor), 0f).toInt()
        val g = Math.max(Color.green(color) * (1 - factor), 0f).toInt()
        val b = Math.max(Color.blue(color) * (1 - factor), 0f).toInt()
        return Color.argb(a, r, g, b)
    }
    
    /**
     * Convert a bitmap to a drawable
     */
    fun bitmapToDrawable(context: Context, bitmap: Bitmap): Drawable {
        return BitmapDrawable(context.resources, bitmap)
    }
} 