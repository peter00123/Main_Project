// =============================================================================
// FILE: PermissionUtils.kt
// Package: com.nearbyshare.utils
// =============================================================================
// INDEX OF CONTENTS:
//   1. getRequiredPermissions() — returns the correct permission list per API
//   2. arePermissionsGranted() — checks if all required permissions are held
//   3. Extension function: Activity.hasPermission()
//   4. Extension function: Context.openAppSettings()
//
// OBJECTIVE:
//   Centralises all runtime-permission logic for NearbyShare.
//   Android's Bluetooth and Wi-Fi permission model changed significantly
//   across API levels (23 → 29 → 31 → 33), making it easy to miss the
//   correct set. This utility file encapsulates that complexity in one place
//   so Activities only need to call getRequiredPermissions() and pass the
//   result to the ActivityResult permission launcher.
// =============================================================================

package com.nearbyshare.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

object PermissionUtils {

    /**
     * Returns the set of permissions required for NearbyShare on the
     * current device's API level.
     *
     * This handles the three main eras of Android permission changes:
     *
     *   API 23–28 (Android 6–8): Classic Bluetooth + coarse/fine location
     *   API 29–30 (Android 10–11): Fine location mandatory for BLE scanning
     *   API 31–32 (Android 12):   New BLUETOOTH_SCAN/CONNECT/ADVERTISE model
     *   API 33+   (Android 13):   NEARBY_WIFI_DEVICES + READ_MEDIA_*
     *
     * @return List of permission strings to pass to the permission launcher.
     */
    fun getRequiredPermissions(): List<String> = buildList {

        // Fine location required for BLE scanning on all APIs (the OS uses
        // location to prevent covert device tracking via BLE)
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // ── API 31+ (Android 12) ─────────────────────────────────────
            // New granular Bluetooth permissions replace the old BLUETOOTH
            // and BLUETOOTH_ADMIN permissions entirely.
            add(Manifest.permission.BLUETOOTH_SCAN)      // BLE discovery
            add(Manifest.permission.BLUETOOTH_CONNECT)   // Connect to paired devices
            add(Manifest.permission.BLUETOOTH_ADVERTISE) // Broadcast BLE advertisements
        } else {
            // ── API 23–30 (Android 6–11) ─────────────────────────────────
            // Legacy Bluetooth permissions (automatically granted from API 28
            // but still needed for the PackageManager declaration)
            add(Manifest.permission.BLUETOOTH)
            add(Manifest.permission.BLUETOOTH_ADMIN)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // ── API 33+ (Android 13) ─────────────────────────────────────
            // Granular media read permissions replace READ_EXTERNAL_STORAGE
            add(Manifest.permission.READ_MEDIA_IMAGES)
            add(Manifest.permission.READ_MEDIA_VIDEO)
            add(Manifest.permission.READ_MEDIA_AUDIO)
            // Wi-Fi peer discovery without needing location access
            add(Manifest.permission.NEARBY_WIFI_DEVICES)
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            // ── API 23–32 ─────────────────────────────────────────────────
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        // Write external storage only needed below Android 10 (API 29)
        // because scoped storage is mandatory from API 29 onwards
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    /**
     * Checks whether all permissions in [permissions] are currently granted.
     *
     * @param context     Any context (Activity or Application).
     * @param permissions List of Manifest.permission.* strings to check.
     * @return true if every permission is PERMISSION_GRANTED, false otherwise.
     */
    fun arePermissionsGranted(context: Context, permissions: List<String>): Boolean =
        permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    /**
     * Convenience: checks whether all REQUIRED permissions for NearbyShare
     * are granted on the current device.
     */
    fun areAllRequiredPermissionsGranted(context: Context): Boolean =
        arePermissionsGranted(context, getRequiredPermissions())
}

// ── Extension Functions ───────────────────────────────────────────────────────

/**
 * Extension on [Activity] to check a single permission quickly.
 *
 * Usage:
 *   if (hasPermission(Manifest.permission.BLUETOOTH_SCAN)) { ... }
 */
fun Activity.hasPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

/**
 * Opens the app's system settings page so the user can manually grant
 * permissions that were permanently denied (Don't ask again).
 *
 * Usage: context.openAppSettings()
 */
fun Context.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(intent)
}
