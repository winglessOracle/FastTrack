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
import wesseling.io.fasttime.ui.theme.*
import java.io.File
import java.io.FileOutputStream
import wesseling.io.fasttime.util.BitmapPool

/**
 * Helper class to create and manage widget background drawables
 */
object WidgetBackgroundHelper {
    private const val TAG = "WidgetBackgroundHelper"
    
    // In-memory cache for background drawables
    private val memoryCache = object : LruCache<String, Drawable>(8) { // Cache up to 8 drawables
        override fun entryRemoved(evicted: Boolean, key: String, oldValue: Drawable, newValue: Drawable?) {
            // When a drawable is removed from cache, try to recover its bitmap
            if (oldValue is BitmapDrawable) {
                val bitmap = oldValue.bitmap
                if (bitmap != null && !bitmap.isRecycled) {
                    // Add the bitmap to our pool for reuse
                    BitmapPool.getInstance().recycleBitmap(bitmap)
                    Log.d(TAG, "Bitmap from drawable recycled to pool from memory cache: $key")
                }
            }
        }
    }
    
    // Memory cache for background bitmaps
    // Use 1/8th of available memory for this cache
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8
    private val bitmapCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            // The cache size will be measured in kilobytes
            return bitmap.byteCount / 1024
        }
        
        // When a bitmap is removed from cache, add it to the bitmap pool for reuse
        override fun entryRemoved(evicted: Boolean, key: String, oldValue: Bitmap, newValue: Bitmap?) {
            if (!oldValue.isRecycled) {
                // Instead of letting GC handle the bitmap, offer it to our bitmap pool
                BitmapPool.getInstance().recycleBitmap(oldValue)
                Log.d(TAG, "Bitmap removed from cache and added to pool: $key")
            }
        }
    }
    
    // Cache cleanup - call this when the app is in the background
    fun clearMemoryCache() {
        bitmapCache.evictAll()
        memoryCache.evictAll()
        
        // Also clear the bitmap pool when under memory pressure
        BitmapPool.getInstance().clear()
        
        Log.d(TAG, "Memory cache and bitmap pool cleared")
    }
    
    /**
     * Get the color for the current fasting state
     */
    fun getColorForFastingState(fastingState: FastingState, context: Context): Int {
        val preferencesManager = PreferencesManager.getInstance(context)
        val themePreference = preferencesManager.dateTimePreferences.themePreference
        
        // Determine if dark theme is being used
        val useDarkTheme = when (themePreference) {
            ThemePreference.SYSTEM -> {
                val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                nightModeFlags == Configuration.UI_MODE_NIGHT_YES
            }
            ThemePreference.LIGHT -> false
            ThemePreference.DARK -> true
        }
        
        // Get the color for the current fasting state
        val colorString = when (fastingState) {
            FastingState.NOT_FASTING -> if (useDarkTheme) NotFastingGrayDark.toHexString() else NotFastingGray.toHexString()
            FastingState.EARLY_FAST -> if (useDarkTheme) EarlyFastingYellowDark.toHexString() else EarlyFastingYellow.toHexString()
            FastingState.GLYCOGEN_DEPLETION -> if (useDarkTheme) GlycogenDepletionOrangeDark.toHexString() else GlycogenDepletionOrange.toHexString()
            FastingState.METABOLIC_SHIFT -> if (useDarkTheme) MetabolicShiftBlueDark.toHexString() else MetabolicShiftBlue.toHexString()
            FastingState.DEEP_KETOSIS -> if (useDarkTheme) DeepKetosisGreenDark.toHexString() else DeepKetosisGreen.toHexString()
            FastingState.IMMUNE_RESET -> if (useDarkTheme) ImmuneResetPurpleDark.toHexString() else ImmuneResetPurple.toHexString()
            FastingState.EXTENDED_FAST -> if (useDarkTheme) ExtendedFastMagentaDark.toHexString() else ExtendedFastMagenta.toHexString()
        }
        
        return Color.parseColor(colorString)
    }
    
    /**
     * Get a background drawable for the widget based on the fasting state
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
            // Check memory cache first (fastest)
            val cachedDrawable = memoryCache.get(cacheKey)
            if (cachedDrawable != null) {
                Log.d(TAG, "Using memory-cached background: $cacheKey")
                return cachedDrawable
            }
            
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
            
            // Create a custom BitmapDrawable
            val drawable = BitmapDrawable(context.resources, bitmap)
            
            // Add a callback to the LruCache to recycle bitmap when evicted
            memoryCache.put(cacheKey, drawable)
            
            return drawable
        } catch (e: Exception) {
            Log.e(TAG, "Error creating background drawable", e)
            
            // Return a simple colored drawable as fallback
            val fallbackBitmap = BitmapPool.getInstance().getBitmap(10, 10, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(fallbackBitmap)
            canvas.drawColor(stateColor)
            return BitmapDrawable(context.resources, fallbackBitmap)
        }
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
        // Try to find a reusable bitmap first
        val bitmap = BitmapPool.getInstance().findReusableBitmap(width, height, Bitmap.Config.ARGB_8888)
            ?: BitmapPool.getInstance().getBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        // Clear the bitmap
        bitmap.eraseColor(0)
        
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
     * Clean up old cache files that haven't been accessed recently
     */
    fun cleanupCacheFiles(context: Context) {
        try {
            val cacheDir = File(context.cacheDir, "widget_backgrounds")
            if (cacheDir.exists()) {
                val files = cacheDir.listFiles()
                if (files != null) {
                    val currentTime = System.currentTimeMillis()
                    val oneDay = 24 * 60 * 60 * 1000L
                    
                    var deletedCount = 0
                    for (file in files) {
                        if (currentTime - file.lastModified() > oneDay) {
                            // Delete files older than one day
                            if (file.delete()) {
                                deletedCount++
                            }
                        }
                    }
                    
                    Log.d(TAG, "Cleaned up $deletedCount cached background files")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up cache files", e)
        }
    }
    
    /**
     * Get a bitmap from disk cache
     */
    private fun getDiskCachedBitmap(context: Context, cacheKey: String): Bitmap? {
        val cacheDir = File(context.cacheDir, "widget_backgrounds")
        val cacheFile = File(cacheDir, "$cacheKey.png")
        
        if (!cacheFile.exists()) {
            return null
        }
        
        try {
            return android.graphics.BitmapFactory.decodeFile(cacheFile.absolutePath)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading bitmap from disk cache", e)
            return null
        }
    }
    
    /**
     * Cache a bitmap to disk
     */
    private fun cacheBitmapToDisk(context: Context, bitmap: Bitmap, cacheKey: String) {
        val cacheDir = File(context.cacheDir, "widget_backgrounds")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        
        val cacheFile = File(cacheDir, "$cacheKey.png")
        
        try {
            FileOutputStream(cacheFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            Log.d(TAG, "Bitmap cached to disk: $cacheKey")
            
            // Don't recycle bitmap here, as we're still using it
            // We'll let the BitmapPool handle recycling when it's no longer needed
        } catch (e: Exception) {
            Log.e(TAG, "Error caching bitmap to disk", e)
        }
    }
    
    /**
     * Get or create a drawable for the widget background
     */
    fun getWidgetBackground(context: Context, fastingState: FastingState, widgetId: Int, width: Int, height: Int): Drawable {
        val cacheKey = "${widgetId}_${width}_${height}_${fastingState.name}"
        
        // Try to get from memory cache first
        memoryCache.get(cacheKey)?.let {
            Log.d(TAG, "Background drawable found in memory cache: $cacheKey")
            return it
        }
        
        // Check disk cache
        val cachedBitmap = getDiskCachedBitmap(context, cacheKey)
        if (cachedBitmap != null) {
            val drawable = BitmapDrawable(context.resources, cachedBitmap)
            Log.d(TAG, "Background bitmap found in disk cache: $cacheKey")
            // Add to memory cache
            memoryCache.put(cacheKey, drawable)
            return drawable
        }
        
        // Create new background drawable
        val color = getColorForFastingState(fastingState, context)
        val bitmap = createRoundedRectBitmap(width, height, color)
        
        // Cache the bitmap
        bitmapCache.put(cacheKey, bitmap)
        cacheBitmapToDisk(context, bitmap, cacheKey)
        
        // Create drawable
        val drawable = BitmapDrawable(context.resources, bitmap)
        
        // Add to memory cache
        memoryCache.put(cacheKey, drawable)
        
        return drawable
    }
    
    /**
     * Create a rounded rectangle bitmap with the specified color
     */
    private fun createRoundedRectBitmap(width: Int, height: Int, color: Int): Bitmap {
        // Ensure minimum dimensions
        val w = if (width <= 0) 100 else width
        val h = if (height <= 0) 100 else height
        
        // Try to find a reusable bitmap first
        val bitmap = BitmapPool.getInstance().findReusableBitmap(w, h, Bitmap.Config.ARGB_8888)
            ?: BitmapPool.getInstance().getBitmap(w, h, Bitmap.Config.ARGB_8888)
        
        // Clear the bitmap
        bitmap.eraseColor(0)
        
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            isAntiAlias = true
            this.color = color
            style = Paint.Style.FILL
        }
        
        // Create rounded rectangle
        val cornerRadius = Math.min(w, h) * 0.1f  // 10% of the smaller dimension
        val rect = RectF(0f, 0f, w.toFloat(), h.toFloat())
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
        
        return bitmap
    }
} 