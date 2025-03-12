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
     * Create a background drawable for the widget based on the fasting state
     * @return Path to the saved drawable file
     */
    fun createBackgroundDrawable(
        context: Context,
        stateColor: Int,
        borderColor: Int,
        borderWidth: Float
    ): String? {
        try {
            // Create a unique filename based on the color and border
            val filename = "widget_bg_${stateColor}_${borderColor}_${borderWidth.toInt()}.png"
            val file = File(context.cacheDir, filename)
            
            // Check if the file already exists
            if (file.exists()) {
                Log.d(TAG, "Using cached background: $filename")
                return file.absolutePath
            }
            
            // Create the bitmap with gradient and border
            // Increased dimensions for better quality
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
            
            // Save the bitmap to a file
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            
            Log.d(TAG, "Created new background: $filename")
            return file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error creating background drawable", e)
            return null
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