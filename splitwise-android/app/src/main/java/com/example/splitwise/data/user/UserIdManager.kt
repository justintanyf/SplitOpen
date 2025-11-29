package com.example.splitwise.data.user

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages user identity persistence.
 * User ID must survive app restarts to preserve "paid by" credits.
 */
@Singleton
class UserIdManager @Inject constructor(@ApplicationContext context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "splitwise_user_prefs",
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_DISPLAY_NAME = "display_name"
    }
    
    /**
     * Get or create persistent user ID.
     */
    fun getUserId(): String {
        var userId = prefs.getString(KEY_USER_ID, null)
        if (userId == null) {
            userId = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_USER_ID, userId).apply()
        }
        return userId
    }
    
    /**
     * Get or set display name.
     */
    fun getDisplayName(): String {
        return prefs.getString(KEY_DISPLAY_NAME, null) ?: "User ${getUserId().takeLast(4)}"
    }
    
    fun setDisplayName(name: String) {
        prefs.edit().putString(KEY_DISPLAY_NAME, name).apply()
    }
}
