package wesseling.io.fasttime.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import wesseling.io.fasttime.model.CompletedFast
import wesseling.io.fasttime.model.FastingState

/**
 * Repository for managing completed fasting sessions
 */
class FastingRepository(context: Context) {
    
    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    private val gson = Gson()
    
    /**
     * Save a completed fast to the repository
     */
    fun saveFast(completedFast: CompletedFast) {
        try {
            val fasts = getAllFasts().toMutableList()
            fasts.add(completedFast)
            
            val json = gson.toJson(fasts)
            prefs.edit().putString(KEY_FASTS, json).commit()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving fast", e)
            throw e
        }
    }
    
    /**
     * Get all completed fasts
     */
    fun getAllFasts(): List<CompletedFast> {
        return try {
            val json = prefs.getString(KEY_FASTS, null) ?: return emptyList()
            
            val type = object : TypeToken<List<CompletedFast>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Error parsing fasts JSON", e)
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all fasts", e)
            emptyList()
        }
    }
    
    /**
     * Get a completed fast by ID
     */
    fun getFastById(id: String): CompletedFast? {
        return try {
            getAllFasts().find { it.id == id }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting fast by ID", e)
            null
        }
    }
    
    /**
     * Delete a completed fast
     */
    fun deleteFast(id: String) {
        try {
            val fasts = getAllFasts().toMutableList()
            fasts.removeIf { it.id == id }
            
            val json = gson.toJson(fasts)
            prefs.edit().putString(KEY_FASTS, json).commit()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting fast", e)
            throw e
        }
    }
    
    /**
     * Delete all completed fasts
     */
    fun deleteAllFasts() {
        try {
            prefs.edit().remove(KEY_FASTS).commit()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting all fasts", e)
            throw e
        }
    }
    
    /**
     * Update a completed fast
     */
    fun updateFast(completedFast: CompletedFast) {
        try {
            val fasts = getAllFasts().toMutableList()
            val index = fasts.indexOfFirst { it.id == completedFast.id }
            
            if (index != -1) {
                fasts[index] = completedFast
                val json = gson.toJson(fasts)
                prefs.edit().putString(KEY_FASTS, json).commit()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating fast", e)
            throw e
        }
    }
    
    companion object {
        private const val TAG = "FastingRepository"
        private const val PREFS_NAME = "wesseling.io.fasttime.fasting_repository"
        private const val KEY_FASTS = "completed_fasts"
        
        @Volatile
        private var instance: FastingRepository? = null
        
        fun getInstance(context: Context): FastingRepository {
            return instance ?: synchronized(this) {
                instance ?: FastingRepository(context).also { instance = it }
            }
        }
    }
} 