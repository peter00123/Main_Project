// ui/home/MainActivity.kt
// Main host activity containing:
//   - Top Toolbar with burger menu button (left) and app name
//   - DrawerLayout with NavigationView (burger menu slide-out panel)
//   - BottomNavigationView (Home | Directory | Profile)
//   - NavHostFragment for fragment navigation
// Burger menu items: About, Settings, Logout
// Logout clears session via utils/SessionManager and returns to LoginActivity
// Depends on: utils/SessionManager (logout), navigation/nav_graph.xml

package com.atezhare.ui.home

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.atezhare.R
import com.atezhare.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Prevent screenshots and screen recording
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupBottomNav()
        handleIntent(intent)
    }

    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: android.content.Intent?) {
        val data = intent?.data
        if (data != null) {
            val raw = data.toString()
            if (raw.startsWith("atezhare://R")) {
                val code = raw.removePrefix("atezhare://R")
                // Pass code to ReceiveFragment via NavController or ViewModel
                val bundle = Bundle().apply {
                    putString("pairing_code", code)
                }
                navController.navigate(R.id.receiveFragment, bundle)
            }
        }
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    private fun setupBottomNav() {
        binding.bottomNav.setupWithNavController(navController)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
