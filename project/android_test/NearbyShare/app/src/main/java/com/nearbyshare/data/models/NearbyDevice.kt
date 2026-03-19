// =============================================================================
// FILE: NearbyDevice.kt
// Package: com.nearbyshare.data.models
// =============================================================================
// INDEX OF CONTENTS:
//   1. DeviceType enum — classifies discovered devices by form factor
//   2. ConnectionState enum — tracks BLE/Wi-Fi connection lifecycle
//   3. NearbyDevice data class — core model representing a discovered peer
//   4. Extension functions for display helpers
//
// OBJECTIVE:
//   Defines the primary data model that represents a remotely discovered
//   Android device visible over Bluetooth Low Energy (BLE) or Wi-Fi Direct.
//   This model flows from the Repository layer up through the ViewModel into
//   the UI (RecyclerView). It is Parcelable so it can be passed between
//   Activities via Intent extras without serialisation overhead.
// =============================================================================

package com.nearbyshare.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Classifies discovered devices by their physical form factor.
 * Used to pick the correct device icon in the UI.
 */
enum class DeviceType {
    PHONE,      // Standard smartphone
    TABLET,     // Tablet / large-screen device
    LAPTOP,     // Chromebook or Android laptop
    WATCH,      // Wear OS smartwatch
    UNKNOWN     // Fallback when type cannot be determined
}

/**
 * Tracks the BLE / Wi-Fi connection lifecycle for a nearby peer.
 * The UI observes this state to show appropriate visual feedback
 * (spinner, checkmark, error icon, etc.).
 */
enum class ConnectionState {
    DISCOVERED,     // Device found during BLE scan; not yet selected
    CONNECTING,     // User tapped the device; connection attempt in progress
    AWAITING_ACCEPT,// Transfer request sent; waiting for remote user to accept
    TRANSFERRING,   // Active data transfer underway
    COMPLETED,      // Transfer finished successfully
    REJECTED,       // Remote user declined the transfer request
    FAILED,         // Connection or transfer error occurred
    DISCONNECTED    // Session ended; device returned to idle state
}

/**
 * Core data model representing a single peer device discovered nearby.
 *
 * @property id              Unique identifier — typically the BLE MAC address or
 *                           a UUID generated from the device's Bluetooth name.
 * @property name            Human-readable device name (e.g. "Pixel 8 Pro").
 * @property deviceType      Form factor classification for icon selection.
 * @property signalStrength  RSSI value from BLE advertisement (-100 to 0 dBm).
 *                           Higher is closer. Used to sort the device list.
 * @property connectionState Current lifecycle state of the connection.
 * @property avatarInitials  1–2 character string derived from device name,
 *                           rendered inside the avatar circle when no image exists.
 * @property isVisible       Whether THIS device is advertising itself as
 *                           discoverable to others (toggled by the user).
 * @property transferProgress Transfer progress percentage (0–100), only
 *                             meaningful when state == TRANSFERRING.
 */
@Parcelize
data class NearbyDevice(
    val id: String,
    val name: String,
    val deviceType: DeviceType = DeviceType.PHONE,
    val signalStrength: Int = -70,          // Default: moderate signal
    val connectionState: ConnectionState = ConnectionState.DISCOVERED,
    val avatarInitials: String = "",
    val isVisible: Boolean = false,
    val transferProgress: Int = 0
) : Parcelable {

    /**
     * Returns a human-readable signal quality label based on RSSI.
     * Nearby Share groups signal into three informal tiers.
     */
    fun signalLabel(): String = when {
        signalStrength >= -55 -> "Close"
        signalStrength >= -70 -> "Nearby"
        else                  -> "Far"
    }

    /**
     * Derives the initials shown inside the avatar circle.
     * Takes the first letter of each word in the device name (max 2 letters).
     * Example: "Pixel 8 Pro" → "PP"
     */
    fun resolvedInitials(): String {
        if (avatarInitials.isNotEmpty()) return avatarInitials.take(2).uppercase()
        return name.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
    }
}
