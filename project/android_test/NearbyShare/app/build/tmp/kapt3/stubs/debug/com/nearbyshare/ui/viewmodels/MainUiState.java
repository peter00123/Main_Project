package com.nearbyshare.ui.viewmodels;

import androidx.lifecycle.ViewModel;
import com.nearbyshare.data.models.*;
import com.nearbyshare.data.repository.INearbyShareRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.*;
import javax.inject.Inject;

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
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0014\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001BM\u0012\u000e\b\u0002\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0006\u0012\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\t\u0012\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u0004\u0012\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\f\u00a2\u0006\u0002\u0010\rJ\u000f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00c6\u0003J\t\u0010\u0018\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u0019\u001a\u00020\u0006H\u00c6\u0003J\u000b\u0010\u001a\u001a\u0004\u0018\u00010\tH\u00c6\u0003J\u000b\u0010\u001b\u001a\u0004\u0018\u00010\u0004H\u00c6\u0003J\u000b\u0010\u001c\u001a\u0004\u0018\u00010\fH\u00c6\u0003JQ\u0010\u001d\u001a\u00020\u00002\u000e\b\u0002\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\u00062\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\t2\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u00042\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\fH\u00c6\u0001J\u0013\u0010\u001e\u001a\u00020\u00062\b\u0010\u001f\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010 \u001a\u00020!H\u00d6\u0001J\t\u0010\"\u001a\u00020\fH\u00d6\u0001R\u0013\u0010\b\u001a\u0004\u0018\u00010\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0013\u0010\u000b\u001a\u0004\u0018\u00010\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0011\u0010\u0007\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\u0012R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0012R\u0017\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0013\u0010\n\u001a\u0004\u0018\u00010\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016\u00a8\u0006#"}, d2 = {"Lcom/nearbyshare/ui/viewmodels/MainUiState;", "", "nearbyDevices", "", "Lcom/nearbyshare/data/models/NearbyDevice;", "isScanning", "", "isDeviceVisible", "activeSession", "Lcom/nearbyshare/data/models/TransferSession;", "selectedDevice", "errorMessage", "", "(Ljava/util/List;ZZLcom/nearbyshare/data/models/TransferSession;Lcom/nearbyshare/data/models/NearbyDevice;Ljava/lang/String;)V", "getActiveSession", "()Lcom/nearbyshare/data/models/TransferSession;", "getErrorMessage", "()Ljava/lang/String;", "()Z", "getNearbyDevices", "()Ljava/util/List;", "getSelectedDevice", "()Lcom/nearbyshare/data/models/NearbyDevice;", "component1", "component2", "component3", "component4", "component5", "component6", "copy", "equals", "other", "hashCode", "", "toString", "app_debug"})
public final class MainUiState {
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.nearbyshare.data.models.NearbyDevice> nearbyDevices = null;
    private final boolean isScanning = false;
    private final boolean isDeviceVisible = false;
    @org.jetbrains.annotations.Nullable()
    private final com.nearbyshare.data.models.TransferSession activeSession = null;
    @org.jetbrains.annotations.Nullable()
    private final com.nearbyshare.data.models.NearbyDevice selectedDevice = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String errorMessage = null;
    
    public MainUiState(@org.jetbrains.annotations.NotNull()
    java.util.List<com.nearbyshare.data.models.NearbyDevice> nearbyDevices, boolean isScanning, boolean isDeviceVisible, @org.jetbrains.annotations.Nullable()
    com.nearbyshare.data.models.TransferSession activeSession, @org.jetbrains.annotations.Nullable()
    com.nearbyshare.data.models.NearbyDevice selectedDevice, @org.jetbrains.annotations.Nullable()
    java.lang.String errorMessage) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.nearbyshare.data.models.NearbyDevice> getNearbyDevices() {
        return null;
    }
    
    public final boolean isScanning() {
        return false;
    }
    
    public final boolean isDeviceVisible() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.nearbyshare.data.models.TransferSession getActiveSession() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.nearbyshare.data.models.NearbyDevice getSelectedDevice() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getErrorMessage() {
        return null;
    }
    
    public MainUiState() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.nearbyshare.data.models.NearbyDevice> component1() {
        return null;
    }
    
    public final boolean component2() {
        return false;
    }
    
    public final boolean component3() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.nearbyshare.data.models.TransferSession component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.nearbyshare.data.models.NearbyDevice component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component6() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.nearbyshare.ui.viewmodels.MainUiState copy(@org.jetbrains.annotations.NotNull()
    java.util.List<com.nearbyshare.data.models.NearbyDevice> nearbyDevices, boolean isScanning, boolean isDeviceVisible, @org.jetbrains.annotations.Nullable()
    com.nearbyshare.data.models.TransferSession activeSession, @org.jetbrains.annotations.Nullable()
    com.nearbyshare.data.models.NearbyDevice selectedDevice, @org.jetbrains.annotations.Nullable()
    java.lang.String errorMessage) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}