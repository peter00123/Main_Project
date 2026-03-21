package com.atezhare.ui.auth

// SplashActivity.kt
// Entry point. Checks if user has an active Supabase session.
// If yes → MainActivity. If no → LoginActivity.
// Also attempts to restore the Supabase session from local storage.
// Depends on: utils/SupabaseClient, utils/SessionManager

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.atezhare.ui.home.MainActivity
import com.atezhare.utils.SessionManager
import com.atezhare.utils.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager(this)

        lifecycleScope.launch {
            try {
                // Try to restore an existing Supabase session
                // This refreshes the token if it has expired
                SupabaseClient.client.auth.awaitInitialization()
                val session = SupabaseClient.client.auth.currentSessionOrNull()

                if (session != null) {
                    // Active Supabase session exists — update stored token
                    val user = SupabaseClient.client.auth.currentUserOrNull()
                    sessionManager.saveSession(user?.id ?: "", session.accessToken)
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                } else {
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                }
            } catch (e: Exception) {
                // Supabase init failed (no internet etc.) — fall back to local check
                if (sessionManager.isLoggedIn()) {
                    sessionManager.restoreSession()
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                } else {
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                }
            }
            finish()
        }
    }
}
