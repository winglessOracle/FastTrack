package wesseling.io.fasttime.util

import android.graphics.Bitmap
import android.util.Log
import android.util.SparseArray
import java.lang.ref.SoftReference
import java.util.LinkedList
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min

/**
 * A bitmap pooling system that allows reusing bitmap memory to reduce GC pressure
 * The pool maintains collections of bitmaps with common configurations to be reused.
 * 
 * Bitmaps are kept in SoftReferences to allow the system to reclaim memory when needed
 * while still allowing reuse when memory is available.
 */
class BitmapPool private constructor() {
    private val TAG = "BitmapPool"
    
    // Map of dimensions to available bitmaps
    // We use size key for quick lookup of appropriate bitmaps
    private val availableBitmaps = ConcurrentHashMap<String, LinkedList<SoftReference<Bitmap>>>()
    
    // Counter to track allocation/reuse stats
    private var totalAllocations = 0
    private var totalReused = 0
    
    /**
     * Create or reuse a bitmap with the requested dimensions and configuration
     * 
     * @param width The requested width
     * @param height The requested height
     * @param config The bitmap configuration (ARGB_8888, RGB_565, etc.)
     * @return A bitmap instance, either reused from the pool or newly allocated
     */
    fun getBitmap(width: Int, height: Int, config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap {
        // Unique key for this configuration (dimensions + pixel format)
        val key = getBitmapKey(width, height, config)
        
        // Try to reuse an existing bitmap from the pool
        synchronized(availableBitmaps) {
            val list = availableBitmaps[key]
            if (list != null && list.isNotEmpty()) {
                // Loop through the list to find a valid bitmap
                val iterator = list.iterator()
                while (iterator.hasNext()) {
                    val bitmapRef = iterator.next()
                    val bitmap = bitmapRef.get()
                    
                    // Remove the reference regardless of whether the bitmap is usable
                    iterator.remove()
                    
                    // If the bitmap is still valid, use it
                    if (bitmap != null && !bitmap.isRecycled) {
                        bitmap.eraseColor(0) // Clear the bitmap contents
                        totalReused++
                        logStats()
                        return bitmap
                    }
                }
            }
        }
        
        // No reusable bitmap found, create a new one
        totalAllocations++
        logStats()
        return Bitmap.createBitmap(width, height, config)
    }
    
    /**
     * Return a bitmap to the pool for future reuse
     * 
     * @param bitmap The bitmap to return to the pool
     * @return true if the bitmap was added to the pool, false otherwise
     */
    fun recycleBitmap(bitmap: Bitmap?): Boolean {
        if (bitmap == null || bitmap.isRecycled) {
            return false
        }
        
        // Get the key for this bitmap
        val key = getBitmapKey(bitmap.width, bitmap.height, bitmap.config)
        
        // Add this bitmap to the pool
        synchronized(availableBitmaps) {
            // Get or create the list for this bitmap size
            val list = availableBitmaps.getOrPut(key) { LinkedList() }
            
            // Add the bitmap to the list, wrapped in a SoftReference
            list.add(SoftReference(bitmap))
            
            // If the list is getting too large, trim it
            // This helps prevent excessive memory usage
            while (list.size > MAX_POOL_SIZE_PER_DIMENSION) {
                list.removeFirst()
            }
        }
        
        return true
    }
    
    /**
     * Clear all pooled bitmaps to free up memory
     */
    fun clear() {
        synchronized(availableBitmaps) {
            for (list in availableBitmaps.values) {
                val iterator = list.iterator()
                while (iterator.hasNext()) {
                    val bitmapRef = iterator.next()
                    val bitmap = bitmapRef.get()
                    if (bitmap != null && !bitmap.isRecycled) {
                        bitmap.recycle()
                    }
                    iterator.remove()
                }
            }
            availableBitmaps.clear()
        }
        
        Log.d(TAG, "Bitmap pool cleared")
    }
    
    /**
     * Log statistics about bitmap allocation and reuse
     */
    private fun logStats() {
        // Only log periodically to reduce spam
        if ((totalAllocations + totalReused) % 10 == 0) {
            val reuseRate = if (totalAllocations + totalReused > 0) {
                (totalReused * 100) / (totalAllocations + totalReused)
            } else 0
            
            Log.d(TAG, "Bitmap stats: allocated=$totalAllocations, reused=$totalReused, " +
                    "reuse rate=$reuseRate%, pool size=${getTotalPoolSize()}")
        }
    }
    
    /**
     * Get the total number of bitmaps in the pool
     */
    private fun getTotalPoolSize(): Int {
        synchronized(availableBitmaps) {
            return availableBitmaps.values.sumOf { it.size }
        }
    }
    
    /**
     * Generate a unique key for a bitmap based on its configuration
     */
    private fun getBitmapKey(width: Int, height: Int, config: Bitmap.Config): String {
        return "${width}x${height}_${config.name}"
    }
    
    /**
     * Find the best matching bitmap that can be reused
     * On API 19+ (KitKat), bitmaps can be reused even if dimensions don't match exactly
     * as long as the new bitmap size is smaller than or equal to the old one
     *
     * @param width The requested width
     * @param height The requested height
     * @param config The bitmap configuration
     * @return A bitmap that can be reused, or null if none is available
     */
    fun findReusableBitmap(width: Int, height: Int, config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap? {
        val targetBytes = width * height * bitsPerPixel(config) / 8
        
        synchronized(availableBitmaps) {
            // First try exact match
            val exactKey = getBitmapKey(width, height, config)
            val exactMatch = findUsableBitmapInList(availableBitmaps[exactKey])
            if (exactMatch != null) {
                return exactMatch
            }
            
            // If we can't find an exact match, look for any bitmap that's big enough
            for ((key, bitmaps) in availableBitmaps) {
                // Only consider bitmaps with the same config
                if (!key.endsWith("_${config.name}")) continue
                
                // Check if any bitmap in this list can be reused
                val bitmap = findUsableBitmapInList(bitmaps)
                if (bitmap != null) {
                    // Check if this bitmap is large enough
                    val bitmapBytes = bitmap.allocationByteCount
                    if (bitmapBytes >= targetBytes) {
                        return bitmap
                    }
                }
            }
        }
        
        return null
    }
    
    /**
     * Find a usable bitmap in a list of soft references
     * 
     * @param list The list of bitmap references to search
     * @return The first usable bitmap, or null if none found
     */
    private fun findUsableBitmapInList(list: LinkedList<SoftReference<Bitmap>>?): Bitmap? {
        if (list == null || list.isEmpty()) return null
        
        val iterator = list.iterator()
        while (iterator.hasNext()) {
            val bitmapRef = iterator.next()
            val bitmap = bitmapRef.get()
            
            if (bitmap != null && !bitmap.isRecycled) {
                // Remove it from the list as we're going to use it
                iterator.remove()
                return bitmap
            } else {
                // Remove invalid references
                iterator.remove()
            }
        }
        
        return null
    }
    
    /**
     * Calculate bits per pixel for a given bitmap configuration
     */
    private fun bitsPerPixel(config: Bitmap.Config): Int {
        return when (config) {
            Bitmap.Config.ARGB_8888 -> 32
            Bitmap.Config.RGB_565 -> 16
            @Suppress("DEPRECATION")
            Bitmap.Config.ARGB_4444 -> 16  // Deprecated but handle for completeness
            Bitmap.Config.ALPHA_8 -> 8
            else -> 32  // Default for unknown configs
        }
    }
    
    /**
     * Trim the bitmap pool to the specified size
     * 
     * @param maxSize Maximum size in bytes to keep in the pool
     */
    fun trim(maxSize: Int) {
        var currentSize = 0
        val sizesMap = mutableMapOf<String, Int>() // Track size per category
        
        synchronized(availableBitmaps) {
            // Calculate current size and build the sizes map
            for ((key, list) in availableBitmaps) {
                var listBytes = 0
                val iterator = list.iterator()
                while (iterator.hasNext()) {
                    val bitmapRef = iterator.next()
                    val bitmap = bitmapRef.get()
                    
                    if (bitmap != null && !bitmap.isRecycled) {
                        listBytes += bitmap.allocationByteCount
                    } else {
                        // Remove invalid references
                        iterator.remove()
                    }
                }
                
                sizesMap[key] = listBytes
                currentSize += listBytes
            }
            
            // If we're under the limit, no need to trim
            if (currentSize <= maxSize) return
            
            // Sort keys by size (largest first)
            val sortedKeys = sizesMap.keys.sortedByDescending { sizesMap[it] }
            
            // Trim each category proportionally
            val excessBytes = currentSize - maxSize
            var bytesRemoved = 0
            
            for (key in sortedKeys) {
                if (bytesRemoved >= excessBytes) break
                
                val list = availableBitmaps[key] ?: continue
                val targetRemoval = (excessBytes * sizesMap[key]!!) / currentSize
                
                // Remove bitmaps until we've freed enough memory
                var removedFromCategory = 0
                while (removedFromCategory < targetRemoval && list.isNotEmpty()) {
                    val bitmapRef = list.removeFirst()
                    val bitmap = bitmapRef.get()
                    
                    if (bitmap != null && !bitmap.isRecycled) {
                        removedFromCategory += bitmap.allocationByteCount
                        bitmap.recycle()
                    }
                }
                
                bytesRemoved += removedFromCategory
            }
            
            Log.d(TAG, "Trimmed bitmap pool: removed $bytesRemoved bytes, " +
                    "current size: ${currentSize - bytesRemoved} bytes")
        }
    }
    
    companion object {
        private const val MAX_POOL_SIZE_PER_DIMENSION = 4 // Maximum number of bitmaps per dimension
        
        @Volatile
        private var INSTANCE: BitmapPool? = null
        
        /**
         * Get the singleton instance of the BitmapPool
         */
        fun getInstance(): BitmapPool {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: BitmapPool().also { INSTANCE = it }
            }
        }
    }
} 