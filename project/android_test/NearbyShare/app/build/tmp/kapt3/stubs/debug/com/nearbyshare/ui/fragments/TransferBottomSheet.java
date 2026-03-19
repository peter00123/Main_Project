package com.nearbyshare.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.Lifecycle;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.nearbyshare.data.models.SharePayload;
import com.nearbyshare.databinding.BottomsheetTransferBinding;
import com.nearbyshare.ui.viewmodels.MainViewModel;
import dagger.hilt.android.AndroidEntryPoint;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\\\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\b\u0007\u0018\u0000 \'2\u00020\u0001:\u0001\'B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0012\u001a\u00020\u0013H\u0002J$\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u00172\b\u0010\u0018\u001a\u0004\u0018\u00010\u00192\b\u0010\u001a\u001a\u0004\u0018\u00010\u001bH\u0016J\b\u0010\u001c\u001a\u00020\u0013H\u0016J\u001a\u0010\u001d\u001a\u00020\u00132\u0006\u0010\u001e\u001a\u00020\u00152\b\u0010\u001a\u001a\u0004\u0018\u00010\u001bH\u0016J\b\u0010\u001f\u001a\u00020\u0013H\u0002J\u0012\u0010 \u001a\u0004\u0018\u00010!2\u0006\u0010\"\u001a\u00020#H\u0002J\u0010\u0010$\u001a\u00020%2\u0006\u0010\"\u001a\u00020#H\u0002J\b\u0010&\u001a\u00020\u0013H\u0002R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0005\u001a\u00020\u00048BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0006\u0010\u0007R\u001c\u0010\b\u001a\u0010\u0012\f\u0012\n \u000b*\u0004\u0018\u00010\n0\n0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001b\u0010\f\u001a\u00020\r8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0010\u0010\u0011\u001a\u0004\b\u000e\u0010\u000f\u00a8\u0006("}, d2 = {"Lcom/nearbyshare/ui/fragments/TransferBottomSheet;", "Lcom/google/android/material/bottomsheet/BottomSheetDialogFragment;", "()V", "_binding", "Lcom/nearbyshare/databinding/BottomsheetTransferBinding;", "binding", "getBinding", "()Lcom/nearbyshare/databinding/BottomsheetTransferBinding;", "filePickerLauncher", "Landroidx/activity/result/ActivityResultLauncher;", "Landroid/content/Intent;", "kotlin.jvm.PlatformType", "viewModel", "Lcom/nearbyshare/ui/viewmodels/MainViewModel;", "getViewModel", "()Lcom/nearbyshare/ui/viewmodels/MainViewModel;", "viewModel$delegate", "Lkotlin/Lazy;", "observeSelectedDevice", "", "onCreateView", "Landroid/view/View;", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onDestroyView", "onViewCreated", "view", "openFilePicker", "resolveFileName", "", "uri", "Landroid/net/Uri;", "resolveFileSize", "", "setupClickListeners", "Companion", "app_debug"})
public final class TransferBottomSheet extends com.google.android.material.bottomsheet.BottomSheetDialogFragment {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String TAG = "TransferBottomSheet";
    @org.jetbrains.annotations.Nullable()
    private com.nearbyshare.databinding.BottomsheetTransferBinding _binding;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy viewModel$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> filePickerLauncher = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.nearbyshare.ui.fragments.TransferBottomSheet.Companion Companion = null;
    
    public TransferBottomSheet() {
        super();
    }
    
    private final com.nearbyshare.databinding.BottomsheetTransferBinding getBinding() {
        return null;
    }
    
    private final com.nearbyshare.ui.viewmodels.MainViewModel getViewModel() {
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
    
    private final void setupClickListeners() {
    }
    
    /**
     * Launches the system document picker allowing multi-select.
     * The ACTION_OPEN_DOCUMENT intent is preferred over ACTION_GET_CONTENT
     * because it grants persistent URI permissions.
     */
    private final void openFilePicker() {
    }
    
    /**
     * Shows the selected device's name at the top of the sheet
     * so the user knows who they're sending to.
     */
    private final void observeSelectedDevice() {
    }
    
    private final java.lang.String resolveFileName(android.net.Uri uri) {
        return null;
    }
    
    private final long resolveFileSize(android.net.Uri uri) {
        return 0L;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0005\u001a\u00020\u0006R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lcom/nearbyshare/ui/fragments/TransferBottomSheet$Companion;", "", "()V", "TAG", "", "newInstance", "Lcom/nearbyshare/ui/fragments/TransferBottomSheet;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.nearbyshare.ui.fragments.TransferBottomSheet newInstance() {
            return null;
        }
    }
}