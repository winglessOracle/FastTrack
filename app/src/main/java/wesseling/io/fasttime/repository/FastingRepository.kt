package wesseling.io.fasttime.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import wesseling.io.fasttime.model.CompletedFast
import wesseling.io.fasttime.model.FastingState

/**
 * Repository for managing completed fasting sessions
 */
class FastingRepository(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    private val gson = Gson()
    
    /**
     * Save a completed fast to the repository
     */
    fun saveFast(completedFast: CompletedFast) {
        val fasts = getAllFasts().toMutableList()
        fasts.add(completedFast)
        
        val json = gson.toJson(fasts)
        prefs.edit().putString(KEY_FASTS, json).apply()
    }
    
    /**
     * Get all completed fasts
     */
    fun getAllFasts(): List<CompletedFast> {
        val json = prefs.getString(KEY_FASTS, null) ?: return emptyList()
        
        val type = object : TypeToken<List<CompletedFast>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
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
        val fasts = getAllFasts().toMutableList()
        fasts.removeIf { it.id == id }
        
        val json = gson.toJson(fasts)
        prefs.edit().putString(KEY_FASTS, json).apply()
    }
    
    /**
     * Delete all completed fasts
     */
    fun deleteAllFasts() {
        prefs.edit().remove(KEY_FASTS).apply()
    }
    
    /**
     * Update a completed fast
     */
    fun updateFast(completedFast: CompletedFast) {
        val fasts = getAllFasts().toMutableList()
        val index = fasts.indexOfFirst { it.id == completedFast.id }
        
        if (index != -1) {
            fasts[index] = completedFast
            val json = gson.toJson(fasts)
            prefs.edit().putString(KEY_FASTS, json).apply()
        }
    }
    
    companion object {
        private const val PREFS_NAME = "wesseling.io.fasttime.FastingRepository"
        private const val KEY_FASTS = "completed_fasts"
    }
} 