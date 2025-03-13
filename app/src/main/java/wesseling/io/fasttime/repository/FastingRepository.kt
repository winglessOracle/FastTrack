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
import wesseling.io.fasttime.model.CompletedFast
import wesseling.io.fasttime.model.FastingState
import java.lang.reflect.Type

/**
 * Repository for managing completed fasting sessions
 */
class FastingRepository(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    // Create a custom Gson instance with type adapters for FastingState
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(FastingState::class.java, FastingStateTypeAdapter())
        .create()
    
    /**
     * Save a completed fast to the repository
     */
    fun saveFast(completedFast: CompletedFast) {
        try {
            Log.d(TAG, "Saving fast: id=${completedFast.id}, duration=${completedFast.durationMillis}, state=${completedFast.maxFastingState}")
            
            val fasts = getAllFasts().toMutableList()
            Log.d(TAG, "Current fasts count: ${fasts.size}")
            
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
     * Get all completed fasts
     */
    fun getAllFasts(): List<CompletedFast> {
        try {
            val json = prefs.getString(KEY_FASTS, null)
            if (json == null) {
                Log.d(TAG, "No fasts found in preferences")
                return emptyList()
            }
            
            Log.d(TAG, "Retrieved JSON size: ${json.length} characters")
            
            val type = object : TypeToken<List<CompletedFast>>() {}.type
            val fasts = gson.fromJson<List<CompletedFast>>(json, type) ?: emptyList()
            
            Log.d(TAG, "Retrieved ${fasts.size} fasts")
            return fasts
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all fasts: ${e.message}", e)
            return emptyList()
        }
    }
    
    /**
     * Get a completed fast by ID
     */
    fun getFastById(id: String): CompletedFast? {
        return getAllFasts().find { it.id == id }
    }
    
    /**
     * Delete a completed fast
     */
    fun deleteFast(id: String) {
        try {
            Log.d(TAG, "Deleting fast with id: $id")
            
            val fasts = getAllFasts().toMutableList()
            val initialSize = fasts.size
            
            fasts.removeIf { it.id == id }
            
            val json = gson.toJson(fasts)
            val editor = prefs.edit()
            editor.putString(KEY_FASTS, json)
            val success = editor.commit()
            
            Log.d(TAG, "Delete result: $success, removed ${initialSize - fasts.size} fasts")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting fast: ${e.message}", e)
        }
    }
    
    /**
     * Delete all completed fasts
     */
    fun deleteAllFasts() {
        try {
            Log.d(TAG, "Deleting all fasts")
            
            val editor = prefs.edit()
            editor.remove(KEY_FASTS)
            val success = editor.commit()
            
            Log.d(TAG, "Delete all result: $success")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting all fasts: ${e.message}", e)
        }
    }
    
    /**
     * Update a completed fast
     */
    fun updateFast(completedFast: CompletedFast) {
        try {
            Log.d(TAG, "Updating fast: id=${completedFast.id}")
            
            val fasts = getAllFasts().toMutableList()
            val index = fasts.indexOfFirst { it.id == completedFast.id }
            
            if (index != -1) {
                fasts[index] = completedFast
                val json = gson.toJson(fasts)
                val editor = prefs.edit()
                editor.putString(KEY_FASTS, json)
                val success = editor.commit()
                
                Log.d(TAG, "Update result: $success")
            } else {
                Log.e(TAG, "Fast not found for update: id=${completedFast.id}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating fast: ${e.message}", e)
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
                Log.e(TAG, "Error deserializing FastingState: ${json.asString}", e)
                FastingState.NOT_FASTING // Default value if deserialization fails
            }
        }
    }
    
    companion object {
        private const val TAG = "FastingRepository"
        private const val PREFS_NAME = "wesseling.io.fasttime.fasting_repository_v2"
        private const val KEY_FASTS = "completed_fasts_v2"
        
        @Volatile
        private var instance: FastingRepository? = null
        
        /**
         * Get the singleton instance of FastingRepository
         */
        fun getInstance(context: Context): FastingRepository {
            return instance ?: synchronized(this) {
                instance ?: FastingRepository(context.applicationContext).also { instance = it }
            }
        }
    }
} 