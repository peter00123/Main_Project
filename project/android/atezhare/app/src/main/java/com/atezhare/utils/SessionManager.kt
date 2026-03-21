// utils/SessionManager.kt
// Manages user authentication session using SharedPreferences.
// Persists login state between app launches.
// Used by: ui/auth/SplashActivity (session check), ui/auth/LoginViewModel (save session),
//          ui/home/MainActivity (get user info), network/RetrofitClient (token via SessionTokenHolder)

package com.atezhare.utils

import android.content.Context
import android.content.SharedPreferences
import com.atezhare.network.SessionTokenHolder

class SessionManager(context: Context) {

    companion object {
        private const val PREF_NAME = "atezhare_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_TOKEN = "token"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /** Save session after successful login. Also updates SessionTokenHolder for Retrofit. */
    fun saveSession(userId: String, token: String?) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_ID, userId)
            putString(KEY_TOKEN, token)
            apply()
        }
        // Sync token to Retrofit interceptor — see network/RetrofitClient
        SessionTokenHolder.token = token
    }

    /** Returns true if a valid session exists (checked in SplashActivity) */
    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    /** Returns the stored userId — used in API requests as senderId/receiverId */
    fun getUserId(): String = prefs.getString(KEY_USER_ID, "") ?: ""

    /** Returns the stored auth token */
    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    /** Clears all session data — called from burger menu logout option in MainActivity */
    fun clearSession() {
        prefs.edit().clear().apply()
        SessionTokenHolder.token = null
    }

    /** Restores token to Retrofit on app restart — call in SplashActivity */
    fun restoreSession() {
        SessionTokenHolder.token = getToken()
    }
}
