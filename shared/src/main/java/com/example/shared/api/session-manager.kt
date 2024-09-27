package com.example.shared.api

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SessionManager private constructor(context: Context) {

    private val sharedPreferences: SharedPreferences

    init {
        // Create or retrieve the Master Key for encryption/decryption
        val masterKey = MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        // Initialize EncryptedSharedPreferences
        sharedPreferences = EncryptedSharedPreferences.create(
            context.applicationContext,
            "session_preferences",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Stores the sessionId securely.
     *
     * @param sessionId The session ID to be stored.
     */
    fun setSessionId(sessionId: String) {
        sharedPreferences.edit().putString("session_id", sessionId).apply()
    }

    /**
     * Retrieves the stored sessionId.
     *
     * @return The session ID if it exists, or null otherwise.
     */
    fun getSessionId(): String? {
        return sharedPreferences.getString("session_id", null)
    }

    /**
     * Removes the stored sessionId.
     */
    fun removeSessionId() {
        sharedPreferences.edit().remove("session_id").apply()
    }

    companion object {
        @Volatile
        private var instance: SessionManager? = null

        /**
         * Initializes the SessionManager with the application context.
         * Should be called once in the Application class.
         */
        fun initialize(context: Context) {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = SessionManager(context)
                    }
                }
            }
        }

        /**
         * Provides the singleton instance of SessionManager.
         * Ensure initialize() is called before calling getInstance().
         */
        fun getInstance(): SessionManager {
            return instance ?: throw IllegalStateException("SessionManager is not initialized.")
        }
    }
}
