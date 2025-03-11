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
            FastingState.NOT_FASTING -> Color.parseColor("#EF4444") // Red
            FastingState.EARLY_FAST -> Color.parseColor("#F59E0B") // Yellow
            FastingState.KETOSIS -> Color.parseColor("#3B82F6") // Blue
            FastingState.AUTOPHAGY -> Color.parseColor("#059669") // Green
            FastingState.DEEP_FASTING -> Color.parseColor("#8B5CF6") // Purple
        }
    }
    
    /**
     * Create a background drawable for the widget based on the fasting state
     * @return Path to the saved drawable file
     */
    fun createBackgroundDrawable(context: Context, state: FastingState, isRunning: Boolean = false): String? {
        try {
            val color = getColorForFastingState(state)
            val bitmap = if (isRunning) {
                createRoundedRectBitmapWithBorder(300, 40, 16f, color, Color.parseColor("#4CAF50"), 3f)
            } else {
                createRoundedRectBitmap(300, 40, 16f, color)
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
     * Create a bitmap with rounded corners
     */
    private fun createRoundedRectBitmap(width: Int, height: Int, cornerRadius: Float, color: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            isAntiAlias = true
            this.color = color
            style = Paint.Style.FILL
        }
        
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
        
        // Add a subtle white stroke
        val strokePaint = Paint().apply {
            isAntiAlias = true
            this.color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 1f
            alpha = 51 // 20% opacity
        }
        
        val strokeRect = RectF(0.5f, 0.5f, width - 0.5f, height - 0.5f)
        canvas.drawRoundRect(strokeRect, cornerRadius, cornerRadius, strokePaint)
        
        return bitmap
    }
    
    /**
     * Create a bitmap with rounded corners and a colored border
     */
    private fun createRoundedRectBitmapWithBorder(
        width: Int, 
        height: Int, 
        cornerRadius: Float, 
        fillColor: Int,
        borderColor: Int,
        borderWidth: Float
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Draw the fill
        val fillPaint = Paint().apply {
            isAntiAlias = true
            color = fillColor
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
     * Convert a bitmap to a drawable
     */
    fun bitmapToDrawable(context: Context, bitmap: Bitmap): Drawable {
        return BitmapDrawable(context.resources, bitmap)
    }
} 