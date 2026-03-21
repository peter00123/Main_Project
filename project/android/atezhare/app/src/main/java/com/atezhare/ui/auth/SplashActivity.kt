package com.atezhare.ui.auth

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
                // Wait for Supabase to load saved session from disk
                SupabaseClient.client.auth.awaitInitialization()

                val session = SupabaseClient.client.auth.currentSessionOrNull()

                if (session != null) {
                    val user = SupabaseClient.client.auth.currentUserOrNull()

                    // CRITICAL — save fresh token into SessionTokenHolder
                    // so RetrofitClient interceptor can attach it to requests
                    sessionManager.saveSession(
                        userId = user?.id ?: "",
                        token  = session.accessToken
                    )

                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                } else {
                    // No active Supabase session — go to login
                    sessionManager.clearSession()
                    startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
                }
            } catch (e: Exception) {
                // Supabase unreachable — go to login
                sessionManager.clearSession()
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            }
            finish()
        }
    }
}
