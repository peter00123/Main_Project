package com.nearbyshare.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.GridLayoutManager;
import com.nearbyshare.R;
import com.nearbyshare.databinding.FragmentHomeBinding;
import com.nearbyshare.ui.adapters.DevicesAdapter;
import com.nearbyshare.ui.viewmodels.MainViewModel;
import dagger.hilt.android.AndroidEntryPoint;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0010\u001a\u00020\u0011H\u0002J$\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u00152\b\u0010\u0016\u001a\u0004\u0018\u00010\u00172\b\u0010\u0018\u001a\u0004\u0018\u00010\u0019H\u0016J\b\u0010\u001a\u001a\u00020\u0011H\u0016J\u001a\u0010\u001b\u001a\u00020\u00112\u0006\u0010\u001c\u001a\u00020\u00132\b\u0010\u0018\u001a\u0004\u0018\u00010\u0019H\u0016J\b\u0010\u001d\u001a\u00020\u0011H\u0002J\b\u0010\u001e\u001a\u00020\u0011H\u0002R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0005\u001a\u00020\u00048BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0006\u0010\u0007R\u000e\u0010\b\u001a\u00020\tX\u0082.\u00a2\u0006\u0002\n\u0000R\u001b\u0010\n\u001a\u00020\u000b8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000e\u0010\u000f\u001a\u0004\b\f\u0010\r\u00a8\u0006\u001f"}, d2 = {"Lcom/nearbyshare/ui/fragments/HomeFragment;", "Landroidx/fragment/app/Fragment;", "()V", "_binding", "Lcom/nearbyshare/databinding/FragmentHomeBinding;", "binding", "getBinding", "()Lcom/nearbyshare/databinding/FragmentHomeBinding;", "devicesAdapter", "Lcom/nearbyshare/ui/adapters/DevicesAdapter;", "viewModel", "Lcom/nearbyshare/ui/viewmodels/MainViewModel;", "getViewModel", "()Lcom/nearbyshare/ui/viewmodels/MainViewModel;", "viewModel$delegate", "Lkotlin/Lazy;", "observeViewModel", "", "onCreateView", "Landroid/view/View;", "inflater", "Landroid/view/LayoutInflater;", "container", "Landroid/view/ViewGroup;", "savedInstanceState", "Landroid/os/Bundle;", "onDestroyView", "onViewCreated", "view", "setupClickListeners", "setupRecyclerView", "app_debug"})
public final class HomeFragment extends androidx.fragment.app.Fragment {
    @org.jetbrains.annotations.Nullable()
    private com.nearbyshare.databinding.FragmentHomeBinding _binding;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy viewModel$delegate = null;
    private com.nearbyshare.ui.adapters.DevicesAdapter devicesAdapter;
    
    public HomeFragment() {
        super();
    }
    
    private final com.nearbyshare.databinding.FragmentHomeBinding getBinding() {
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
    
    /**
     * Configures the RecyclerView with a 2-column grid layout and
     * attaches the DevicesAdapter. Device taps are forwarded to the
     * ViewModel which triggers the transfer flow.
     */
    private final void setupRecyclerView() {
    }
    
    /**
     * Wires all button click and switch change listeners.
     */
    private final void setupClickListeners() {
    }
    
    /**
     * Collects UI state updates from the ViewModel using the
     * repeatOnLifecycle pattern, which automatically stops collection
     * when the Fragment is stopped and resumes when it starts again.
     * This prevents unnecessary work and avoids crashes from
     * accessing a destroyed view.
     */
    private final void observeViewModel() {
    }
}