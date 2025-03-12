package wesseling.io.fasttime.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import wesseling.io.fasttime.model.FastingState
import java.io.File
import java.io.FileOutputStream

/**
 * Helper class to create and manage widget background drawables
 */
object WidgetBackgroundHelper {
    private const val TAG = "WidgetBackgroundHelper"
    
    /**
     * Get color for fasting state
     */
    fun getColorForFastingState(state: FastingState): Int {
        return when (state) {
            FastingState.NOT_FASTING -> Color.parseColor("#757575") // Neutral gray
            FastingState.EARLY_FAST -> Color.parseColor("#F59E0B") // Amber
            FastingState.GLYCOGEN_DEPLETION -> Color.parseColor("#EA580C") // Orange
            FastingState.METABOLIC_SHIFT -> Color.parseColor("#3B82F6") // Blue
            FastingState.DEEP_KETOSIS -> Color.parseColor("#059669") // Green
            FastingState.IMMUNE_RESET -> Color.parseColor("#8B5CF6") // Purple
            FastingState.EXTENDED_FAST -> Color.parseColor("#DB2777") // Magenta
        }
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