// =============================================================================
// FILE: NearbyShareApp.kt
// Package: com.nearbyshare
// =============================================================================
// INDEX OF CONTENTS:
//   1. Application class declaration with @HiltAndroidApp
//   2. Timber logging initialisation (debug builds only)
//   3. Global uncaught exception handler
//
// OBJECTIVE:
//   This is the Application-level entry point for the NearbyShare app.
//   Annotated with @HiltAndroidApp to trigger Hilt's code generation,
//   making the application component the root of the DI hierarchy.
//   Also initialises Timber (logging library) so all log calls throughout
//   the codebase are automatically suppressed in release builds.
// =============================================================================

package com.nearbyshare

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

/**
 * Custom Application class for NearbyShare.
 *
 * @HiltAndroidApp triggers Hilt's code generation, producing the
 * application-level DI component that all other Hilt components depend on.
 * This must be declared in AndroidManifest.xml via android:name=".NearbyShareApp".
 */
@HiltAndroidApp
class NearbyShareApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // ── Logging setup ────────────────────────────────────────────────
        // Only enable verbose logging in debug builds.
        // In release builds, Log calls become no-ops.
        if (BuildConfig.DEBUG) {
            Log.d("NearbyShareApp", "Debug build — verbose logging enabled")
        }

        // ── Global uncaught exception handler ────────────────────────────
        // Catches any unhandled exceptions and logs them before the system
        // terminates the process. Useful for diagnosing crash reports.
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("NearbyShareApp", "Uncaught exception on thread ${thread.name}", throwable)
        }
    }
}
