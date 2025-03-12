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
    fun createBackgroundDrawable(context: Context, state: FastingState, isRunning: Boolean = false): String? {
        try {
            val color = getColorForFastingState(state)
            val darkerColor = darkenColor(color, 0.2f)
            
            val bitmap = if (isRunning) {
                // When running, add a green border
                createRoundedRectBitmapWithGradient(
                    300, 40, 16f, 
                    color, darkerColor,
                    Color.parseColor("#4CAF50"), 3f
                )
            } else {
                // Normal state with gradient
                createRoundedRectBitmapWithGradient(300, 40, 16f, color, darkerColor)
            }
            
            // Save bitmap to file
            val fileName = if (isRunning) {
                "widget_bg_${state.name.lowercase()}_running.png"
            } else {
                "widget_bg_${state.name.lowercase()}.png"
            }
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            
            return file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error creating background drawable", e)
            return null
        }
    }
    
    /**
     * Create a bitmap with rounded corners and gradient
     */
    private fun createRoundedRectBitmapWithGradient(
        width: Int, 
        height: Int, 
        cornerRadius: Float, 
        startColor: Int,
        endColor: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Create gradient paint
        val paint = Paint().apply {
            isAntiAlias = true
            shader = android.graphics.LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                startColor, endColor,
                android.graphics.Shader.TileMode.CLAMP
            )
            style = Paint.Style.FILL
        }
        
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
        
        // Add a subtle white stroke
        val strokePaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            alpha = 60 // 24% opacity
        }
        
        val strokeRect = RectF(0.75f, 0.75f, width - 0.75f, height - 0.75f)
        canvas.drawRoundRect(strokeRect, cornerRadius - 0.75f, cornerRadius - 0.75f, strokePaint)
        
        return bitmap
    }
    
    /**
     * Create a bitmap with rounded corners, gradient and a colored border
     */
    private fun createRoundedRectBitmapWithGradient(
        width: Int, 
        height: Int, 
        cornerRadius: Float, 
        startColor: Int,
        endColor: Int,
        borderColor: Int,
        borderWidth: Float
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