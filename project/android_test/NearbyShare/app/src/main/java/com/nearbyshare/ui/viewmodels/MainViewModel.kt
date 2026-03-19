// =============================================================================
// FILE: MainViewModel.kt
// Package: com.nearbyshare.ui.viewmodels
// =============================================================================
// INDEX OF CONTENTS:
//   1. MainViewModel class declaration with @HiltViewModel
//   2. UI State data class (encapsulates all state for the main screen)
//   3. Observed StateFlows from the repository
//   4. Event handlers (startScan, stopScan, toggleVisibility, selectDevice)
//   5. Transfer initiation logic
//
// OBJECTIVE:
//   The ViewModel for MainActivity and its hosted fragments.
//   Follows the Unidirectional Data Flow (UDF) pattern:
//     Repository → ViewModel (StateFlow) → UI (Fragment observes)
//     UI (user action) → ViewModel (event function) → Repository (suspend call)
//
//   This ViewModel survives configuration changes (screen rotation) and
//   exposes only immutable StateFlow/SharedFlow to the UI, preventing
//   the Fragment from directly mutating state.
// =============================================================================

package com.nearbyshare.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearbyshare.data.models.*
import com.nearbyshare.data.repository.INearbyShareRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UI State ──────────────────────────────────────────────────────────────────

/**
 * Immutable snapshot of everything the main screen needs to render.
 *
 * @property nearbyDevices    Current list of BLE-discovered devices.
 * @property isScanning       True while the BLE scan is active.
 * @property isDeviceVisible  True when this device is advertising itself.
 * @property activeSession    The current or most recent transfer session, or null.
 * @property selectedDevice   Device the user tapped (pre-transfer state).
 * @property errorMessage     Non-null when an error should be shown as a Snackbar.
 */
data class MainUiState(
    val nearbyDevices: List<NearbyDevice> = emptyList(),
    val isScanning: Boolean = false,
    val isDeviceVisible: Boolean = false,
    val activeSession: TransferSession? = null,
    val selectedDevice: NearbyDevice? = null,
    val errorMessage: String? = null
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

/**
 * @HiltViewModel marks this for Hilt injection.
 * The [INearbyShareRepository] is provided by [AppModule].
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: INearbyShareRepository
) : ViewModel() {

    // ── Private mutable backing state ────────────────────────────────────────
    private val _uiState = MutableStateFlow(MainUiState())

    /**
     * The UI observes this immutable StateFlow.
     * It is a combination of repository flows and local ViewModel state.
     */
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // ── One-time navigation events ────────────────────────────────────────────
    // SharedFlow (replay=0) ensures the event fires exactly once per observer.
    private val _navigateToTransfer = MutableSharedFlow<TransferSession>()
    val navigateToTransfer: SharedFlow<TransferSession> = _navigateToTransfer.asSharedFlow()

    // ── Init: subscribe to repository flows ───────────────────────────────────
    init {
        // Collect discovered devices and propagate to UI state
        viewModelScope.launch {
            repository.nearbyDevices.collect { devices ->
                _uiState.update { it.copy(nearbyDevices = devices) }
            }
        }

        // Collect active session updates
        viewModelScope.launch {
            repository.activeSession.collect { session ->
                _uiState.update { it.copy(activeSession = session) }
            }
        }

        // Collect device visibility toggle state
        viewModelScope.launch {
            repository.isDeviceVisible.collect { visible ->
                _uiState.update { it.copy(isDeviceVisible = visible) }
            }
        }
    }

    // ============================================================
    // User Actions
    // ============================================================

    /**
     * Called when the user taps the "Scan" / refresh button.
     * Marks scanning = true then delegates to the repository.
     * If an error occurs it is surfaced as an errorMessage in UI state.
     */
    fun startScan() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isScanning = true, errorMessage = null) }
                repository.startDiscovery()
                // Scanning is async; set to false once the list starts populating
                _uiState.update { it.copy(isScanning = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isScanning    = false,
                        errorMessage  = "Could not start scan: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    /**
     * Stops the ongoing BLE scan and clears the device list.
     */
    fun stopScan() {
        viewModelScope.launch {
            repository.stopDiscovery()
            _uiState.update { it.copy(isScanning = false) }
        }
    }

    /**
     * Toggles whether this device broadcasts a BLE advertisement.
     * Called when the user taps the visibility switch on the main screen.
     */
    fun toggleVisibility() {
        val current = _uiState.value.isDeviceVisible
        repository.setDeviceVisible(!current)
    }

    /**
     * Called when the user taps a device in the RecyclerView.
     * Stores the selected device so the TransferBottomSheet can reference it.
     */
    fun selectDevice(device: NearbyDevice) {
        _uiState.update { it.copy(selectedDevice = device) }
    }

    /**
     * Initiates a send operation to the currently selected device.
     *
     * @param payload The content to transfer (file, text, URL).
     */
    fun sendPayload(payload: SharePayload) {
        val target = _uiState.value.selectedDevice ?: run {
            _uiState.update { it.copy(errorMessage = "No device selected") }
            return
        }

        viewModelScope.launch {
            try {
                val sessionId = repository.sendTo(target, payload)
                // Emit a navigation event to open the transfer progress screen
                repository.activeSession.filterNotNull().first { it.sessionId == sessionId }
                    .also { _navigateToTransfer.emit(it) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Transfer failed: ${e.localizedMessage}")
                }
            }
        }
    }

    /**
     * Cancels the current active transfer.
     */
    fun cancelActiveTransfer() {
        viewModelScope.launch {
            _uiState.value.activeSession?.sessionId?.let {
                repository.cancelTransfer(it)
            }
        }
    }

    /**
     * Clears the error message after it has been displayed to the user.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
