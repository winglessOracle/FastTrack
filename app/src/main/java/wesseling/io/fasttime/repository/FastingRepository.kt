package wesseling.io.fasttime.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import wesseling.io.fasttime.R
import wesseling.io.fasttime.model.CompletedFast
import wesseling.io.fasttime.model.FastingState
import wesseling.io.fasttime.util.FastingValidator
import java.lang.reflect.Type
import java.util.UUID

/**
 * Repository for handling storage and retrieval of completed fast entries
 */
class FastingRepository private constructor(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // Set up Gson with a type adapter for FastingState
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(FastingState::class.java, FastingStateTypeAdapter())
        .create()
    
    /**
     * Save a completed fast to storage
     * 
     * @param completedFast The fast entry to save
     * @param skipValidation Whether to skip overlap validation (default: false)
     * @throws IllegalArgumentException if fast validation fails
     */
    fun saveFast(completedFast: CompletedFast, skipValidation: Boolean = false) {
        try {
            Log.d(TAG, "Saving fast: id=${completedFast.id}, duration=${completedFast.durationMillis}, state=${completedFast.maxFastingState}")
            
            // Get existing fasts
            val fasts = getAllFasts().toMutableList()
            Log.d(TAG, "Current fasts count: ${fasts.size}")
            
            // Validate the fast entry for overlaps
            if (!skipValidation) {
                val validationResult = FastingValidator.checkForOverlappingFasts(context, completedFast, fasts)
                if (!validationResult.isValid) {
                    val errorMsg = validationResult.errorMessage ?: context.getString(R.string.error_invalid_fast)
                    Log.e(TAG, "Validation failed: $errorMsg")
                    throw IllegalArgumentException(errorMsg)
                }
            }
            
            // Add the fast
            fasts.add(completedFast)
            
            val json = gson.toJson(fasts)
            Log.d(TAG, "JSON size: ${json.length} characters")
            
            val editor = prefs.edit()
            editor.putString(KEY_FASTS, json)
            val success = editor.commit() // Use commit() instead of apply() for immediate result
            
            if (!success) {
                Log.e(TAG, "Failed to commit changes to SharedPreferences")
                throw RuntimeException("Failed to commit changes to SharedPreferences")
            }
            
            // Verify the save worked
            val savedFasts = getAllFasts()
            Log.d(TAG, "Fasts after save: ${savedFasts.size}")
            val savedFast = savedFasts.find { it.id == completedFast.id }
            if (savedFast != null) {
                Log.d(TAG, "Fast was saved successfully")
            } else {
                Log.e(TAG, "Fast was not saved!")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving fast: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * Get all saved fast entries
     */
    fun getAllFasts(): List<CompletedFast> {
        return try {
            val json = prefs.getString(KEY_FASTS, "[]")
            val type = object : TypeToken<List<CompletedFast>>() {}.type
            val fasts = gson.fromJson<List<CompletedFast>>(json, type) ?: emptyList()
            
            // Sort by end time (newest first)
            fasts.sortedByDescending { it.endTimeMillis }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading fasts: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Delete a fast entry by ID
     */
    fun deleteFast(fastId: String) {
        try {
            Log.d(TAG, "Deleting fast: id=$fastId")
            
            val fasts = getAllFasts().toMutableList()
            val initialSize = fasts.size
            fasts.removeIf { it.id == fastId }
            val finalSize = fasts.size
            
            if (initialSize == finalSize) {
                Log.w(TAG, "Fast not found for deletion: id=$fastId")
                return
            }
            
            val json = gson.toJson(fasts)
            val editor = prefs.edit()
            editor.putString(KEY_FASTS, json)
            editor.apply()
            
            Log.d(TAG, "Fast deleted successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting fast: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * Delete all fast entries
     */
    fun deleteAllFasts() {
        try {
            Log.d(TAG, "Deleting all fasts")
            
            val editor = prefs.edit()
            editor.putString(KEY_FASTS, "[]")
            editor.apply()
            
            Log.d(TAG, "All fasts deleted successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting all fasts: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * Update an existing fast entry
     * 
     * @param completedFast The updated fast entry
     * @param skipValidation Whether to skip overlap validation (default: false)
     * @throws IllegalArgumentException if fast validation fails or fast not found
     */
    fun updateFast(completedFast: CompletedFast, skipValidation: Boolean = false) {
        try {
            Log.d(TAG, "Updating fast: id=${completedFast.id}")
            
            val fasts = getAllFasts().toMutableList()
            val index = fasts.indexOfFirst { it.id == completedFast.id }
            
            if (index != -1) {
                // Validate the fast entry for overlaps, skipping the current fast ID
                if (!skipValidation) {
                    val validationResult = FastingValidator.checkForOverlappingFasts(
                        context,
                        completedFast, 
                        fasts,
                        skipFastId = completedFast.id
                    )
                    
                    if (!validationResult.isValid) {
                        val errorMsg = validationResult.errorMessage ?: context.getString(R.string.error_invalid_fast)
                        Log.e(TAG, "Validation failed during update: $errorMsg")
                        throw IllegalArgumentException(errorMsg)
                    }
                }
                
                // Update the fast
                fasts[index] = completedFast
                val json = gson.toJson(fasts)
                val editor = prefs.edit()
                editor.putString(KEY_FASTS, json)
                val success = editor.commit()
                
                Log.d(TAG, "Update result: $success")
            } else {
                Log.e(TAG, "Fast not found for update: id=${completedFast.id}")
                throw IllegalArgumentException(context.getString(R.string.error_fast_not_found))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating fast: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * Custom type adapter for FastingState enum to ensure proper serialization/deserialization
     */
    private class FastingStateTypeAdapter : JsonSerializer<FastingState>, JsonDeserializer<FastingState> {
        override fun serialize(src: FastingState, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return JsonPrimitive(src.name)
        }
        
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): FastingState {
            return try {
                FastingState.valueOf(json.asString)
            } catch (e: Exception) {
                Log.e(TAG, "Error deserializing FastingState: ${e.message}", e)
                FastingState.NOT_FASTING
            }
        }
    }
    
    companion object {
        private const val TAG = "FastingRepository"
        private const val PREFS_NAME = "wesseling.io.fasttime.fasting_prefs"
        private const val KEY_FASTS = "fasts"
        
        @Volatile
        private var INSTANCE: FastingRepository? = null
        
        fun getInstance(context: Context): FastingRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = FastingRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
} 