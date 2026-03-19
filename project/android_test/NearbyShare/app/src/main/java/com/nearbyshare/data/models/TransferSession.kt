// =============================================================================
// FILE: TransferSession.kt
// Package: com.nearbyshare.data.models
// =============================================================================
// INDEX OF CONTENTS:
//   1. TransferDirection enum — SENDING vs RECEIVING
//   2. TransferStatus enum — full lifecycle of a transfer session
//   3. TransferSession data class — tracks a live or historical transfer
//
// OBJECTIVE:
//   Models a single file-transfer session between this device and a peer.
//   A session is created the moment the user taps a device to send, or
//   the moment an incoming transfer request arrives. The ViewModel observes
//   a Flow<TransferSession> emitted by the Repository and updates the UI
//   (progress bar, status label, speed indicator) accordingly.
//   Sessions are persisted to a local Room database so the user can see
//   transfer history even after the app restarts.
// =============================================================================

package com.nearbyshare.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Whether this device is sending or receiving in this session.
 */
enum class TransferDirection {
    SENDING,    // This device initiated the share
    RECEIVING   // This device accepted an incoming share
}

/**
 * Full lifecycle states of a transfer session, used to drive
 * UI transitions (spinner → progress bar → success/error screen).
 */
enum class TransferStatus {
    PENDING,            // Session created; waiting for connection
    CONNECTING,         // Establishing Wi-Fi / BLE channel
    AWAITING_ACCEPT,    // Sender side: waiting for receiver to tap "Accept"
    AWAITING_USER,      // Receiver side: showing Accept/Decline dialog
    TRANSFERRING,       // Bytes actively flowing
    COMPLETED,          // All bytes delivered successfully
    DECLINED,           // Receiver tapped "Decline"
    CANCELLED,          // Either side cancelled mid-transfer
    FAILED              // Network error or unexpected disconnection
}

/**
 * Represents one complete file-transfer session.
 *
 * @property sessionId      Unique identifier for this transfer session.
 * @property remoteDevice   The peer device on the other side of the transfer.
 * @property payload        The content being transferred (file, text, URL).
 * @property direction      Whether we are the sender or receiver.
 * @property status         Current lifecycle state.
 * @property bytesTransferred How many bytes have been sent/received so far.
 * @property totalBytes     Total bytes to transfer (mirrors payload.totalBytes).
 * @property speedBytesPerSec Current transfer speed, updated ~1 Hz.
 * @property startedAtMs    System.currentTimeMillis() when transfer started.
 * @property completedAtMs  System.currentTimeMillis() when transfer completed (or 0).
 * @property errorMessage   Human-readable error description if status == FAILED.
 */
@Parcelize
data class TransferSession(
    val sessionId: String,
    val remoteDevice: NearbyDevice,
    val payload: SharePayload,
    val direction: TransferDirection,
    val status: TransferStatus = TransferStatus.PENDING,
    val bytesTransferred: Long = 0L,
    val totalBytes: Long = 0L,
    val speedBytesPerSec: Long = 0L,
    val startedAtMs: Long = 0L,
    val completedAtMs: Long = 0L,
    val errorMessage: String = ""
) : Parcelable {

    /**
     * Returns the transfer progress as an integer percentage 0–100.
     * Guarded against division-by-zero when totalBytes is not yet known.
     */
    val progressPercent: Int
        get() = if (totalBytes > 0) ((bytesTransferred * 100) / totalBytes).toInt().coerceIn(0, 100)
                else 0

    /**
     * Formats the current transfer speed as a human-readable string.
     * Example output: "2.4 MB/s", "850 KB/s"
     */
    fun formattedSpeed(): String = when {
        speedBytesPerSec <= 0              -> ""
        speedBytesPerSec < 1_024           -> "${speedBytesPerSec} B/s"
        speedBytesPerSec < 1_048_576       -> "${"%.0f".format(speedBytesPerSec / 1_024.0)} KB/s"
        else                               -> "${"%.1f".format(speedBytesPerSec / 1_048_576.0)} MB/s"
    }

    /**
     * Estimates time remaining based on current speed and bytes left.
     * Returns an empty string if speed is unknown or transfer is done.
     */
    fun estimatedTimeRemaining(): String {
        if (speedBytesPerSec <= 0 || status != TransferStatus.TRANSFERRING) return ""
        val remaining = totalBytes - bytesTransferred
        val seconds = remaining / speedBytesPerSec
        return when {
            seconds < 60   -> "${seconds}s left"
            seconds < 3600 -> "${seconds / 60}m left"
            else           -> "${seconds / 3600}h left"
        }
    }

    /** Returns true when the session has reached a terminal state. */
    val isTerminal: Boolean
        get() = status in listOf(
            TransferStatus.COMPLETED,
            TransferStatus.DECLINED,
            TransferStatus.CANCELLED,
            TransferStatus.FAILED
        )
}
