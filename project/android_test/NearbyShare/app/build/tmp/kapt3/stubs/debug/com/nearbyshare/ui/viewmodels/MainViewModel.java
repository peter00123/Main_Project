package com.nearbyshare.ui.viewmodels;

import androidx.lifecycle.ViewModel;
import com.nearbyshare.data.models.*;
import com.nearbyshare.data.repository.INearbyShareRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.*;
import javax.inject.Inject;

/**
 * @HiltViewModel marks this for Hilt injection.
 * The [INearbyShareRepository] is provided by [AppModule].
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u0013\u001a\u00020\u0014J\u0006\u0010\u0015\u001a\u00020\u0014J\u000e\u0010\u0016\u001a\u00020\u00142\u0006\u0010\u0017\u001a\u00020\u0018J\u000e\u0010\u0019\u001a\u00020\u00142\u0006\u0010\u001a\u001a\u00020\u001bJ\u0006\u0010\u001c\u001a\u00020\u0014J\u0006\u0010\u001d\u001a\u00020\u0014J\u0006\u0010\u001e\u001a\u00020\u0014R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\u00070\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\n0\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012\u00a8\u0006\u001f"}, d2 = {"Lcom/nearbyshare/ui/viewmodels/MainViewModel;", "Landroidx/lifecycle/ViewModel;", "repository", "Lcom/nearbyshare/data/repository/INearbyShareRepository;", "(Lcom/nearbyshare/data/repository/INearbyShareRepository;)V", "_navigateToTransfer", "Lkotlinx/coroutines/flow/MutableSharedFlow;", "Lcom/nearbyshare/data/models/TransferSession;", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/nearbyshare/ui/viewmodels/MainUiState;", "navigateToTransfer", "Lkotlinx/coroutines/flow/SharedFlow;", "getNavigateToTransfer", "()Lkotlinx/coroutines/flow/SharedFlow;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "cancelActiveTransfer", "", "clearError", "selectDevice", "device", "Lcom/nearbyshare/data/models/NearbyDevice;", "sendPayload", "payload", "Lcom/nearbyshare/data/models/SharePayload;", "startScan", "stopScan", "toggleVisibility", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class MainViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.nearbyshare.data.repository.INearbyShareRepository repository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.nearbyshare.ui.viewmodels.MainUiState> _uiState = null;
    
    /**
     * The UI observes this immutable StateFlow.
     * It is a combination of repository flows and local ViewModel state.
     */
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.nearbyshare.ui.viewmodels.MainUiState> uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableSharedFlow<com.nearbyshare.data.models.TransferSession> _navigateToTransfer = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.SharedFlow<com.nearbyshare.data.models.TransferSession> navigateToTransfer = null;
    
    @javax.inject.Inject()
    public MainViewModel(@org.jetbrains.annotations.NotNull()
    com.nearbyshare.data.repository.INearbyShareRepository repository) {
        super();
    }
    
    /**
     * The UI observes this immutable StateFlow.
     * It is a combination of repository flows and local ViewModel state.
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.nearbyshare.ui.viewmodels.MainUiState> getUiState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.SharedFlow<com.nearbyshare.data.models.TransferSession> getNavigateToTransfer() {
        return null;
    }
    
    /**
     * Called when the user taps the "Scan" / refresh button.
     * Marks scanning = true then delegates to the repository.
     * If an error occurs it is surfaced as an errorMessage in UI state.
     */
    public final void startScan() {
    }
    
    /**
     * Stops the ongoing BLE scan and clears the device list.
     */
    public final void stopScan() {
    }
    
    /**
     * Toggles whether this device broadcasts a BLE advertisement.
     * Called when the user taps the visibility switch on the main screen.
     */
    public final void toggleVisibility() {
    }
    
    /**
     * Called when the user taps a device in the RecyclerView.
     * Stores the selected device so the TransferBottomSheet can reference it.
     */
    public final void selectDevice(@org.jetbrains.annotations.NotNull()
    com.nearbyshare.data.models.NearbyDevice device) {
    }
    
    /**
     * Initiates a send operation to the currently selected device.
     *
     * @param payload The content to transfer (file, text, URL).
     */
    public final void sendPayload(@org.jetbrains.annotations.NotNull()
    com.nearbyshare.data.models.SharePayload payload) {
    }
    
    /**
     * Cancels the current active transfer.
     */
    public final void cancelActiveTransfer() {
    }
    
    /**
     * Clears the error message after it has been displayed to the user.
     */
    public final void clearError() {
    }
}