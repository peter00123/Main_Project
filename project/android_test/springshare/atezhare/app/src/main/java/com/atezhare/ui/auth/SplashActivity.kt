// ui/auth/SplashActivity.kt
// Entry point of the app. Checks session state via SessionManager.
// If logged in → navigates to MainActivity (home)
// If not logged in → navigates to LoginActivity
// Depends on: utils/SessionManager (isLoggedIn check)

package com.atezhare.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.atezhare.ui.home.MainActivity
import com.atezhare.utils.SessionManager

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // SessionManager handles SharedPreferences — see utils/SessionManager
        val sessionManager = SessionManager(this)

        // Restore token to Retrofit interceptor if session exists
        sessionManager.restoreSession()

        if (sessionManager.isLoggedIn()) {
            // User has an active session — go to main app
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // No session — prompt login
            startActivity(Intent(this, LoginActivity::class.java))
        }

        finish() // Remove splash from back stack
    }
}
