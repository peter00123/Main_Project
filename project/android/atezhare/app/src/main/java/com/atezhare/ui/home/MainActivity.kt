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

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.atezhare.R
import com.atezhare.databinding.ActivityMainBinding
import com.atezhare.ui.auth.LoginActivity
import com.atezhare.utils.SessionManager
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    // SessionManager handles logout — see utils/SessionManager.clearSession()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupToolbar()
        setupNavigation()
        setupDrawerMenu()
        setupBottomNav()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.app_name)
    }

    private fun setupNavigation() {
        // NavHostFragment hosts all main fragments — see res/navigation/nav_graph.xml
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    private fun setupDrawerMenu() {
        // ActionBarDrawerToggle connects toolbar burger icon to drawer open/close
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)
    }

    private fun setupBottomNav() {
        // BottomNav tied to NavController — navigates between Home, Directory, Profile
        binding.bottomNav.setupWithNavController(navController)
    }

    // Handles burger menu item clicks — see res/menu/drawer_menu.xml
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_about -> {
                // TODO: Navigate to About fragment or show dialog
                showAboutDialog()
            }
            R.id.nav_settings -> {
                // TODO: Navigate to Settings fragment
                navController.navigate(R.id.settingsFragment)
            }
            R.id.nav_logout -> {
                // Clear session and go back to login — see utils/SessionManager
                sessionManager.clearSession()
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showAboutDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("About Atezhare")
            .setMessage("Atezhare v1.0\nSecure peer-to-peer document sharing.\nPowered by Spring Boot backend.")
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onBackPressed() {
        // Close drawer on back press if open
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
