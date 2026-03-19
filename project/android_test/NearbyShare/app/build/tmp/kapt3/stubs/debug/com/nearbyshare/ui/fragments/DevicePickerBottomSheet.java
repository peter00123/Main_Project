package com.nearbyshare.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.nearbyshare.data.models.SharePayload;
import com.nearbyshare.databinding.BottomsheetDevicePickerBinding;
import com.nearbyshare.ui.adapters.DevicesAdapter;
import com.nearbyshare.ui.viewmodels.MainViewModel;
import dagger.hilt.android.AndroidEntryPoint;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\b\u0007\u0018\u0000 $2\u00020\u0001:\u0001$B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0015\u001a\u00020\u0016H\u0002J$\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u001a2\b\u0010\u001b\u001a\u0004\u0018\u00010\u001c2\b\u0010\u001d\u001a\u0004\u0018\u00010\u001eH\u0016J\b\u0010\u001f\u001a\u00020\u0016H\u0016J\u001a\u0010 \u001a\u00020\u00162\u0006\u0010!\u001a\u00020\u00182\b\u0010\u001d\u001a\u0004\u0018\u00010\u001eH\u0016J\b\u0010\"\u001a\u00020\u0016H\u0002J\b\u0010#\u001a\u00020\u0016H\u0002R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0005\u001a\u00020\u00048BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0006\u0010\u0007R\u000e\u0010\b\u001a\u00020\tX\u0082.\u00a2\u0006\u0002\n\u0000R\u001d\u0010\n\u001a\u0004\u0018\u00010\u000b8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000e\u0010\u000f\u001a\u0004\b\f\u0010\rR\u001b\u0010\u0010\u001a\u00020\u00118BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0014\u0010\u000f\u001a\u0004\b\u0012\u0010\u0013\u00a8\u0006%"}, d2 = {"Lcom/nearbyshare/ui/fragments/DevicePickerBottomSheet;", "Lcom/google/android/material/bottomsheet/BottomSheetDialogFragment;", "()V", "_binding", "Lcom/nearbyshare/databinding/BottomsheetDevicePickerBinding;", "binding", "getBinding", "()Lcom/nearbyshare/databinding/BottomsheetDevicePickerBinding;", "devicesAdapter", "Lcom/nearbyshare/ui/adapters/DevicesAdapter;", "payload", "Lcom/nearbyshare/data/models/SharePayload;", "getPayload", "()Lcom/nearbyshare/data/models/SharePayload;", "payload$delegate", "Lkotlin/Lazy;", "viewModel", "Lcom/nearbyshare/ui/viewmodels/MainViewModel;", "getViewModel", "()Lcom/nearbyshare/ui/viewmodels/MainViewModel;", "viewModel$delegate", "observeDevices", "", "onCreateView", "Landroid/view/View;", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onDestroyView", "onViewCreated", "view", "setupPayloadPreview", "setupRecyclerView", "Companion", "app_debug"})
public final class DevicePickerBottomSheet extends com.google.android.material.bottomsheet.BottomSheetDialogFragment {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String TAG = "DevicePickerBottomSheet";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String ARG_PAYLOAD = "arg_payload";
    @org.jetbrains.annotations.Nullable()
    private com.nearbyshare.databinding.BottomsheetDevicePickerBinding _binding;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy viewModel$delegate = null;
    private com.nearbyshare.ui.adapters.DevicesAdapter devicesAdapter;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy payload$delegate = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.nearbyshare.ui.fragments.DevicePickerBottomSheet.Companion Companion = null;
    
    public DevicePickerBottomSheet() {
        super();
    }
    
    private final com.nearbyshare.databinding.BottomsheetDevicePickerBinding getBinding() {
        return null;
    }
    
    private final com.nearbyshare.ui.viewmodels.MainViewModel getViewModel() {
        return null;
    }
    
    private final com.nearbyshare.data.models.SharePayload getPayload() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public android.view.View onCreateView(@org.jetbrains.annotations.NotNull()
    android.view.LayoutInflater inflater, @org.jetbrains.annotations.Nullable()
    android.view.ViewGroup container, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
        return null;
    }
    
    @java.lang.Override()
    public void onViewCreated(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    @java.lang.Override()
    public void onDestroyView() {
    }
    
    /**
     * Populates the payload preview card at the top of the sheet.
     * Shows the file name, size, and a type-appropriate icon.
     */
    private final void setupPayloadPreview() {
    }
    
    /**
     * Configures the device list RecyclerView.
     * Uses a vertical LinearLayoutManager (list style, not grid)
     * because the bottom sheet has limited vertical space.
     */
    private final void setupRecyclerView() {
    }
    
    private final void observeDevices() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcom/nearbyshare/ui/fragments/DevicePickerBottomSheet$Companion;", "", "()V", "ARG_PAYLOAD", "", "TAG", "newInstance", "Lcom/nearbyshare/ui/fragments/DevicePickerBottomSheet;", "payload", "Lcom/nearbyshare/data/models/SharePayload;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        /**
         * Factory method that bundles the SharePayload as a Fragment argument.
         * Using a factory instead of a public constructor is the Android-recommended
         * pattern — it ensures arguments survive process death/recreation.
         */
        @org.jetbrains.annotations.NotNull()
        public final com.nearbyshare.ui.fragments.DevicePickerBottomSheet newInstance(@org.jetbrains.annotations.NotNull()
        com.nearbyshare.data.models.SharePayload payload) {
            return null;
        }
    }
}