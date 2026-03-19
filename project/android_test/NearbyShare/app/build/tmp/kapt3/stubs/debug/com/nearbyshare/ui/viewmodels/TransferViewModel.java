package com.nearbyshare.ui.viewmodels;

import androidx.lifecycle.ViewModel;
import com.nearbyshare.data.models.TransferSession;
import com.nearbyshare.data.models.TransferStatus;
import com.nearbyshare.data.repository.INearbyShareRepository;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.*;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\f\u001a\u00020\rJ\u0006\u0010\u000e\u001a\u00020\rJ\u0006\u0010\u000f\u001a\u00020\rJ\u0012\u0010\u0010\u001a\u00020\u00072\b\u0010\u0011\u001a\u0004\u0018\u00010\u0012H\u0002R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00070\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000b\u00a8\u0006\u0013"}, d2 = {"Lcom/nearbyshare/ui/viewmodels/TransferViewModel;", "Landroidx/lifecycle/ViewModel;", "repository", "Lcom/nearbyshare/data/repository/INearbyShareRepository;", "(Lcom/nearbyshare/data/repository/INearbyShareRepository;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/nearbyshare/ui/viewmodels/TransferUiState;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "acceptTransfer", "", "cancelTransfer", "declineTransfer", "mapSessionToUiState", "session", "Lcom/nearbyshare/data/models/TransferSession;", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class TransferViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.nearbyshare.data.repository.INearbyShareRepository repository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.nearbyshare.ui.viewmodels.TransferUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.nearbyshare.ui.viewmodels.TransferUiState> uiState = null;
    
    @javax.inject.Inject()
    public TransferViewModel(@org.jetbrains.annotations.NotNull()
    com.nearbyshare.data.repository.INearbyShareRepository repository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.nearbyshare.ui.viewmodels.TransferUiState> getUiState() {
        return null;
    }
    
    /**
     * Converts a raw [TransferSession] into a flat [TransferUiState]
     * with pre-computed display strings and boolean visibility flags.
     *
     * This keeps all formatting logic out of the Fragment,
     * making it easier to unit-test without Android framework.
     */
    private final com.nearbyshare.ui.viewmodels.TransferUiState mapSessionToUiState(com.nearbyshare.data.models.TransferSession session) {
        return null;
    }
    
    /**
     * Accept an incoming file transfer (receiver side).
     */
    public final void acceptTransfer() {
    }
    
    /**
     * Decline an incoming file transfer (receiver side).
     */
    public final void declineTransfer() {
    }
    
    /**
     * Cancel an in-progress transfer from either side.
     */
    public final void cancelTransfer() {
    }
}