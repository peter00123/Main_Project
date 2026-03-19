// =============================================================================
// FILE: MainActivity.kt
// Package: com.nearbyshare.ui.activities
// =============================================================================
// INDEX OF CONTENTS:
//   1. Imports and class declaration with @AndroidEntryPoint
//   2. ViewBinding setup and layout inflation
//   3. Permission request logic (Bluetooth, Location, Storage)
//   4. Navigation host setup (NavController + bottom navigation)
//   5. Snackbar error display
//   6. Lifecycle observation (start scan after permissions granted)
//
// OBJECTIVE:
//   The single-activity host for all NearbyShare screens.
//   Uses the Navigation Component to swap between three destinations:
//     - HomeFragment     (device discovery + visibility toggle)
//     - TransferFragment (progress, accept/decline, completion)
//     - SettingsFragment (device name, visibility mode preferences)
//   Handles runtime permission requests on behalf of child Fragments.
//   Observes the MainViewModel for error messages and navigation events.
// =============================================================================

package com.nearbyshare.ui.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.nearbyshare.R
import com.nearbyshare.databinding.ActivityMainBinding
import com.nearbyshare.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * @AndroidEntryPoint enables Hilt injection for this Activity and all
 * Fragments hosted within it.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // ── ViewBinding — type-safe access to activity_main.xml views ────────────
    private lateinit var binding: ActivityMainBinding

    // ── ViewModel scoped to this Activity lifecycle ───────────────────────────
    private val viewModel: MainViewModel by viewModels()

    // ── NavController — drives fragment back-stack ────────────────────────────
    private lateinit var navController: NavController

    // ============================================================
    // Permission Launcher
    // ============================================================

    /**
     * ActivityResult API launcher for requesting multiple permissions at once.
     * On API 31+ we request the new BLUETOOTH_* permissions; on older devices
     * we fall back to ACCESS_FINE_LOCATION (required for BLE scanning).
     */
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        // Check if all required permissions were granted
        val allGranted = results.values.all { it }
        if (allGranted) {
            // Permissions granted — start scanning immediately
            viewModel.startScan()
        } else {
            // At least one permission denied — show explanation
            showSnackbar("Permissions required for device discovery")
        }
    }

    // ============================================================
    // Lifecycle
    // ============================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout and set as content view
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ── Navigation setup ─────────────────────────────────────────────
        setupNavigation()

        // ── Observe ViewModel state ──────────────────────────────────────
        observeViewModel()

        // ── Request permissions then start scan ──────────────────────────
        requestRequiredPermissions()
    }

    // ============================================================
    // Navigation
    // ============================================================

    /**
     * Wires the NavHostFragment to a NavController and connects it
     * to the bottom navigation bar (Material BottomNavigationView).
     * Each item in the bottom nav corresponds to a destination in
     * res/navigation/nav_graph.xml.
     */
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Sync BottomNavigationView selection with the current destination
        binding.bottomNavigation.setupWithNavController(navController)

        // Hide bottom nav on the transfer screen (immersive progress UI)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigation.visibility = when (destination.id) {
                R.id.transferFragment -> View.GONE
                else                  -> View.VISIBLE
            }
        }
    }

    // ============================================================
    // ViewModel Observation
    // ============================================================

    /**
     * Collects UI state flows in a coroutine tied to the STARTED lifecycle
     * state (auto-cancelled when Activity goes to background).
     */
    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Display Snackbar for any transient error messages
                launch {
                    viewModel.uiState.collect { state ->
                        state.errorMessage?.let { message ->
                            showSnackbar(message)
                            viewModel.clearError()
                        }
                    }
                }

                // Navigate to transfer screen when a session starts
                launch {
                    viewModel.navigateToTransfer.collect {
                        navController.navigate(R.id.transferFragment)
                    }
                }
            }
        }
    }

    // ============================================================
    // Permissions
    // ============================================================

    /**
     * Builds and requests the correct permission set for the running OS version.
     *
     * Android 12+ (API 31): BLUETOOTH_SCAN, BLUETOOTH_CONNECT, BLUETOOTH_ADVERTISE
     * Android 10–11:        ACCESS_FINE_LOCATION (required for BLE scanning)
     * Android 6–9:          ACCESS_FINE_LOCATION + BLUETOOTH / BLUETOOTH_ADMIN
     */
    private fun requestRequiredPermissions() {
        val permissions = buildList {
            // Location — needed on all API levels for Wi-Fi Direct peer discovery
            add(Manifest.permission.ACCESS_FINE_LOCATION)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // API 31+ (Android 12): new Bluetooth permission model
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
                add(Manifest.permission.BLUETOOTH_ADVERTISE)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // API 33+ (Android 13): granular media read permissions
                add(Manifest.permission.READ_MEDIA_IMAGES)
                add(Manifest.permission.READ_MEDIA_VIDEO)
                add(Manifest.permission.NEARBY_WIFI_DEVICES)
            } else {
                // API < 33: legacy storage permission
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        // Filter to only permissions that haven't been granted yet
        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isEmpty()) {
            // All already granted — start scan immediately
            viewModel.startScan()
        } else {
            // Launch the system permission dialog
            permissionLauncher.launch(notGranted.toTypedArray())
        }
    }

    // ============================================================
    // Helpers
    // ============================================================

    /**
     * Displays a brief Snackbar anchored above the bottom navigation bar.
     *
     * @param message The text to display.
     */
    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .setAnchorView(binding.bottomNavigation)
            .show()
    }
}
