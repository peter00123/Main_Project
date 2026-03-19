package com.nearbyshare.ui.viewmodels;

import androidx.lifecycle.ViewModel;
import com.nearbyshare.data.models.TransferSession;
import com.nearbyshare.data.models.TransferStatus;
import com.nearbyshare.data.repository.INearbyShareRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.*;
import javax.inject.Inject;

/**
 * Distilled, display-ready state for the transfer progress screen.
 *
 * @property session           The raw session model (may be null before first update).
 * @property progressPercent   0–100 integer for the ProgressBar.
 * @property formattedSpeed    "2.1 MB/s" or "" if not transferring.
 * @property estimatedTime     "12s left" or "" if not deterministic.
 * @property formattedSize     "3.2 MB" or "" for text payloads.
 * @property showAcceptDecline True on receiver side in AWAITING_USER state.
 * @property showCancel        True while transfer is in progress.
 * @property showDone          True when transfer reaches a terminal state.
 * @property statusLabel       Short human-readable status string.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b \b\u0086\b\u0018\u00002\u00020\u0001Ba\u0012\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u0012\b\b\u0002\u0010\b\u001a\u00020\u0007\u0012\b\b\u0002\u0010\t\u001a\u00020\u0007\u0012\b\b\u0002\u0010\n\u001a\u00020\u000b\u0012\b\b\u0002\u0010\f\u001a\u00020\u000b\u0012\b\b\u0002\u0010\r\u001a\u00020\u000b\u0012\b\b\u0002\u0010\u000e\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\u000fJ\u000b\u0010\u001d\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010\u001e\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u001f\u001a\u00020\u0007H\u00c6\u0003J\t\u0010 \u001a\u00020\u0007H\u00c6\u0003J\t\u0010!\u001a\u00020\u0007H\u00c6\u0003J\t\u0010\"\u001a\u00020\u000bH\u00c6\u0003J\t\u0010#\u001a\u00020\u000bH\u00c6\u0003J\t\u0010$\u001a\u00020\u000bH\u00c6\u0003J\t\u0010%\u001a\u00020\u0007H\u00c6\u0003Je\u0010&\u001a\u00020\u00002\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\u00072\b\b\u0002\u0010\t\u001a\u00020\u00072\b\b\u0002\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\f\u001a\u00020\u000b2\b\b\u0002\u0010\r\u001a\u00020\u000b2\b\b\u0002\u0010\u000e\u001a\u00020\u0007H\u00c6\u0001J\u0013\u0010\'\u001a\u00020\u000b2\b\u0010(\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010)\u001a\u00020\u0005H\u00d6\u0001J\t\u0010*\u001a\u00020\u0007H\u00d6\u0001R\u0011\u0010\b\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0011\u0010\t\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0011R\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0011R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0013\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0011\u0010\n\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0011\u0010\f\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0019R\u0011\u0010\r\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0019R\u0011\u0010\u000e\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u0011\u00a8\u0006+"}, d2 = {"Lcom/nearbyshare/ui/viewmodels/TransferUiState;", "", "session", "Lcom/nearbyshare/data/models/TransferSession;", "progressPercent", "", "formattedSpeed", "", "estimatedTime", "formattedSize", "showAcceptDecline", "", "showCancel", "showDone", "statusLabel", "(Lcom/nearbyshare/data/models/TransferSession;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;ZZZLjava/lang/String;)V", "getEstimatedTime", "()Ljava/lang/String;", "getFormattedSize", "getFormattedSpeed", "getProgressPercent", "()I", "getSession", "()Lcom/nearbyshare/data/models/TransferSession;", "getShowAcceptDecline", "()Z", "getShowCancel", "getShowDone", "getStatusLabel", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "toString", "app_debug"})
public final class TransferUiState {
    @org.jetbrains.annotations.Nullable()
    private final com.nearbyshare.data.models.TransferSession session = null;
    private final int progressPercent = 0;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String formattedSpeed = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String estimatedTime = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String formattedSize = null;
    private final boolean showAcceptDecline = false;
    private final boolean showCancel = false;
    private final boolean showDone = false;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String statusLabel = null;
    
    public TransferUiState(@org.jetbrains.annotations.Nullable()
    com.nearbyshare.data.models.TransferSession session, int progressPercent, @org.jetbrains.annotations.NotNull()
    java.lang.String formattedSpeed, @org.jetbrains.annotations.NotNull()
    java.lang.String estimatedTime, @org.jetbrains.annotations.NotNull()
    java.lang.String formattedSize, boolean showAcceptDecline, boolean showCancel, boolean showDone, @org.jetbrains.annotations.NotNull()
    java.lang.String statusLabel) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.nearbyshare.data.models.TransferSession getSession() {
        return null;
    }
    
    public final int getProgressPercent() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFormattedSpeed() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getEstimatedTime() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFormattedSize() {
        return null;
    }
    
    public final boolean getShowAcceptDecline() {
        return false;
    }
    
    public final boolean getShowCancel() {
        return false;
    }
    
    public final boolean getShowDone() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getStatusLabel() {
        return null;
    }
    
    public TransferUiState() {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.nearbyshare.data.models.TransferSession component1() {
        return null;
    }
    
    public final int component2() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component5() {
        return null;
    }
    
    public final boolean component6() {
        return false;
    }
    
    public final boolean component7() {
        return false;
    }
    
    public final boolean component8() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.nearbyshare.ui.viewmodels.TransferUiState copy(@org.jetbrains.annotations.Nullable()
    com.nearbyshare.data.models.TransferSession session, int progressPercent, @org.jetbrains.annotations.NotNull()
    java.lang.String formattedSpeed, @org.jetbrains.annotations.NotNull()
    java.lang.String estimatedTime, @org.jetbrains.annotations.NotNull()
    java.lang.String formattedSize, boolean showAcceptDecline, boolean showCancel, boolean showDone, @org.jetbrains.annotations.NotNull()
    java.lang.String statusLabel) {
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