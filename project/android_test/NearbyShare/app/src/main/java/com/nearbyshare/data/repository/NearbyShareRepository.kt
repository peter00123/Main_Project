// =============================================================================
// FILE: NearbyShareRepository.kt
// Package: com.nearbyshare.data.repository
// =============================================================================
// INDEX OF CONTENTS:
//   1. Interface definition — INearbyShareRepository (contract)
//   2. NearbyShareRepository implementation
//   3. BLE device discovery simulation (StateFlow of device lists)
//   4. Transfer session management (start, cancel, update progress)
//   5. Device visibility toggle logic
//   6. Hilt binding module
//
// OBJECTIVE:
//   Acts as the single source of truth for all NearbyShare data operations.
//   In a production build this would wrap the Google Nearby Connections API
//   and/or Wi-Fi Direct sockets. Here it exposes realistic StateFlows that
//   the ViewModel collects, simulating BLE device discovery and file transfer
//   progress so the full UI pipeline can be exercised and demonstrated.
//   The repository is injected into ViewModels via Hilt.
// =============================================================================

package com.nearbyshare.data.repository

import com.nearbyshare.data.models.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

// ── Interface (contract) ──────────────────────────────────────────────────────

/**
 * Contract that any data source backing the NearbyShare features must satisfy.
 * Using an interface allows unit tests to inject a fake implementation.
 */
interface INearbyShareRepository {

    /** Flow emitting the current list of discovered nearby devices. */
    val nearbyDevices: StateFlow<List<NearbyDevice>>

    /** Flow emitting the active/most-recent transfer session, or null. */
    val activeSession: StateFlow<TransferSession?>

    /** Flow emitting whether THIS device is currently advertising itself. */
    val isDeviceVisible: StateFlow<Boolean>

    /** Starts scanning for nearby BLE devices. */
    suspend fun startDiscovery()

    /** Stops BLE scanning. */
    suspend fun stopDiscovery()

    /** Toggle whether this device is visible/discoverable to others. */
    fun setDeviceVisible(visible: Boolean)

    /**
     * Initiates a transfer to [targetDevice] with the given [payload].
     * @return The session ID created for this transfer.
     */
    suspend fun sendTo(targetDevice: NearbyDevice, payload: SharePayload): String

    /** Accept an incoming transfer request by [sessionId]. */
    suspend fun acceptTransfer(sessionId: String)

    /** Decline an incoming transfer request by [sessionId]. */
    suspend fun declineTransfer(sessionId: String)

    /** Cancel an ongoing transfer by [sessionId]. */
    suspend fun cancelTransfer(sessionId: String)
}

// ── Implementation ────────────────────────────────────────────────────────────

/**
 * Default repository implementation.
 *
 * Device Discovery:
 *   Simulates BLE advertising packets arriving from nearby Android phones.
 *   In a real app, this would call startDiscovery() on the Google Nearby
 *   Connections API or use BluetoothLeScanner.
 *
 * File Transfer:
 *   Simulates a complete send/receive lifecycle including the "awaiting
 *   accept" pause, transfer progress increments, and completion.
 */
@Singleton
class NearbyShareRepository @Inject constructor() : INearbyShareRepository {

    // ── Coroutine scope ──────────────────────────────────────────────────────
    // SupervisorJob ensures one failing child doesn't cancel siblings.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ── Device visibility ────────────────────────────────────────────────────
    private val _isDeviceVisible = MutableStateFlow(false)
    override val isDeviceVisible: StateFlow<Boolean> = _isDeviceVisible.asStateFlow()

    // ── Discovered devices list ───────────────────────────────────────────────
    private val _nearbyDevices = MutableStateFlow<List<NearbyDevice>>(emptyList())
    override val nearbyDevices: StateFlow<List<NearbyDevice>> = _nearbyDevices.asStateFlow()

    // ── Active transfer session ───────────────────────────────────────────────
    private val _activeSession = MutableStateFlow<TransferSession?>(null)
    override val activeSession: StateFlow<TransferSession?> = _activeSession.asStateFlow()

    // Job handle so we can cancel the discovery coroutine on stopDiscovery()
    private var discoveryJob: Job? = null

    // ── Static mock device pool (simulates neighbourhood BLE advertisements) ─
    private val mockDevicePool = listOf(
        NearbyDevice("dev_001", "Pixel 8 Pro",      DeviceType.PHONE,  -45),
        NearbyDevice("dev_002", "Galaxy S24 Ultra",  DeviceType.PHONE,  -58),
        NearbyDevice("dev_003", "OnePlus 12",        DeviceType.PHONE,  -63),
        NearbyDevice("dev_004", "Redmi Note 13",     DeviceType.PHONE,  -72),
        NearbyDevice("dev_005", "Galaxy Tab S9",     DeviceType.TABLET, -55),
        NearbyDevice("dev_006", "Pixel Tablet",      DeviceType.TABLET, -68),
        NearbyDevice("dev_007", "Chromebook Flex 5", DeviceType.LAPTOP, -75),
        NearbyDevice("dev_008", "Galaxy Watch 6",    DeviceType.WATCH,  -80),
    )

    // ============================================================
    // Discovery
    // ============================================================

    /**
     * Starts simulated BLE discovery.
     * Devices are added to the list progressively (as they would be in
     * a real scan) with slight random delays between appearances.
     */
    override suspend fun startDiscovery() {
        // Cancel any previous discovery job before starting a new one
        stopDiscovery()

        discoveryJob = scope.launch {
            _nearbyDevices.value = emptyList() // Reset the list on each scan

            // Drip-feed mock devices to simulate real BLE scan arrival order
            mockDevicePool.forEachIndexed { index, device ->
                delay(600L + (index * 300L)) // Stagger each device appearance

                // Append device to the current list (immutable copy pattern)
                _nearbyDevices.update { current -> current + device }
            }
        }
    }

    /**
     * Stops BLE scanning and clears the discovered device list.
     */
    override suspend fun stopDiscovery() {
        discoveryJob?.cancel()
        discoveryJob = null
        _nearbyDevices.value = emptyList()
    }

    // ============================================================
    // Device Visibility
    // ============================================================

    /**
     * Toggles this device's BLE advertising (discoverability).
     * When visible == true, nearby devices running NearbyShare will see
     * this device in their own scan results.
     */
    override fun setDeviceVisible(visible: Boolean) {
        _isDeviceVisible.value = visible
    }

    // ============================================================
    // Sending
    // ============================================================

    /**
     * Initiates a file/text send to [targetDevice].
     *
     * The simulated lifecycle:
     *   PENDING → CONNECTING (1s) → AWAITING_ACCEPT (2s) → TRANSFERRING (per-byte)
     *   → COMPLETED
     *
     * @return The UUID of the created [TransferSession].
     */
    override suspend fun sendTo(targetDevice: NearbyDevice, payload: SharePayload): String {
        val sessionId = UUID.randomUUID().toString()

        // Create the initial pending session
        val session = TransferSession(
            sessionId    = sessionId,
            remoteDevice = targetDevice,
            payload      = payload,
            direction    = TransferDirection.SENDING,
            status       = TransferStatus.PENDING,
            totalBytes   = payload.totalBytes.takeIf { it > 0 } ?: 10_000_000L // 10 MB fallback
        )
        _activeSession.value = session

        // Run the transfer simulation on the IO dispatcher
        scope.launch { simulateSendLifecycle(sessionId) }

        return sessionId
    }

    /**
     * Simulates the full sender-side transfer lifecycle as a coroutine.
     * Each stage emits an updated [TransferSession] to [_activeSession].
     */
    private suspend fun simulateSendLifecycle(sessionId: String) {
        fun currentSession() = _activeSession.value?.takeIf { it.sessionId == sessionId }

        // Stage 1: Connecting
        _activeSession.update { it?.copy(status = TransferStatus.CONNECTING) }
        delay(1_200)

        // Stage 2: Waiting for remote user to accept
        _activeSession.update { it?.copy(status = TransferStatus.AWAITING_ACCEPT) }
        delay(2_500)

        // Stage 3: Start transfer
        val session = currentSession() ?: return
        val totalBytes = session.totalBytes
        _activeSession.update {
            it?.copy(status = TransferStatus.TRANSFERRING, startedAtMs = System.currentTimeMillis())
        }

        // Simulate incremental byte progress in 50-tick chunks
        val ticks = 50
        val bytesPerTick = totalBytes / ticks
        repeat(ticks) { tick ->
            delay(80) // ~4 seconds total transfer time
            val transferred = bytesPerTick * (tick + 1)
            val speed = bytesPerTick * (1000 / 80)  // bytes per second estimate
            _activeSession.update {
                it?.copy(
                    bytesTransferred = transferred,
                    speedBytesPerSec = speed
                )
            }
        }

        // Stage 4: Completed
        _activeSession.update {
            it?.copy(
                status           = TransferStatus.COMPLETED,
                bytesTransferred = totalBytes,
                completedAtMs    = System.currentTimeMillis(),
                speedBytesPerSec = 0L
            )
        }
    }

    // ============================================================
    // Receiving
    // ============================================================

    /**
     * Accept an incoming transfer. Transitions session from AWAITING_USER
     * → TRANSFERRING and triggers a receive-side simulation.
     */
    override suspend fun acceptTransfer(sessionId: String) {
        _activeSession.update {
            if (it?.sessionId == sessionId) it.copy(status = TransferStatus.TRANSFERRING)
            else it
        }
        scope.launch { simulateReceiveProgress(sessionId) }
    }

    /**
     * Decline an incoming transfer. Sets status to DECLINED.
     */
    override suspend fun declineTransfer(sessionId: String) {
        _activeSession.update {
            if (it?.sessionId == sessionId) it.copy(status = TransferStatus.DECLINED)
            else it
        }
    }

    /**
     * Cancel an ongoing transfer from either side. Sets status to CANCELLED.
     */
    override suspend fun cancelTransfer(sessionId: String) {
        _activeSession.update {
            if (it?.sessionId == sessionId) it.copy(status = TransferStatus.CANCELLED)
            else it
        }
    }

    /**
     * Simulates incoming data arriving after the user taps "Accept".
     * Mirrors [simulateSendLifecycle] but without the AWAITING_ACCEPT stage.
     */
    private suspend fun simulateReceiveProgress(sessionId: String) {
        val totalBytes = _activeSession.value?.takeIf { it.sessionId == sessionId }
                            ?.totalBytes ?: return

        val ticks = 50
        val bytesPerTick = totalBytes / ticks

        repeat(ticks) { tick ->
            delay(80)
            val transferred = bytesPerTick * (tick + 1)
            _activeSession.update {
                if (it?.sessionId == sessionId) {
                    it.copy(
                        bytesTransferred = transferred,
                        speedBytesPerSec = bytesPerTick * (1000 / 80)
                    )
                } else it
            }
        }

        _activeSession.update {
            if (it?.sessionId == sessionId) {
                it.copy(
                    status           = TransferStatus.COMPLETED,
                    bytesTransferred = totalBytes,
                    completedAtMs    = System.currentTimeMillis(),
                    speedBytesPerSec = 0L
                )
            } else it
        }
    }
}
