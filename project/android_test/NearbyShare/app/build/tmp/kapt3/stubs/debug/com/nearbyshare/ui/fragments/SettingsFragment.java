package com.nearbyshare.ui.fragments;

import android.os.Bundle;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import com.nearbyshare.R;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\u0018\u0000 \f2\u00020\u0001:\u0001\fB\u0005\u00a2\u0006\u0002\u0010\u0002J\u001c\u0010\u0003\u001a\u00020\u00042\b\u0010\u0005\u001a\u0004\u0018\u00010\u00062\b\u0010\u0007\u001a\u0004\u0018\u00010\bH\u0016J\b\u0010\t\u001a\u00020\u0004H\u0002J\b\u0010\n\u001a\u00020\u0004H\u0002J\b\u0010\u000b\u001a\u00020\u0004H\u0002\u00a8\u0006\r"}, d2 = {"Lcom/nearbyshare/ui/fragments/SettingsFragment;", "Landroidx/preference/PreferenceFragmentCompat;", "()V", "onCreatePreferences", "", "savedInstanceState", "Landroid/os/Bundle;", "rootKey", "", "setupDataPref", "setupDeviceNamePref", "setupVisibilityPref", "Companion", "app_debug"})
public final class SettingsFragment extends androidx.preference.PreferenceFragmentCompat {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String PREF_DEVICE_NAME = "pref_device_name";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String PREF_VISIBILITY_MODE = "pref_visibility_mode";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String PREF_WIFI_ONLY = "pref_wifi_only";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String PREF_SHOW_NOTIFICATION = "pref_show_notification";
    @org.jetbrains.annotations.NotNull()
    public static final com.nearbyshare.ui.fragments.SettingsFragment.Companion Companion = null;
    
    public SettingsFragment() {
        super();
    }
    
    @java.lang.Override()
    public void onCreatePreferences(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState, @org.jetbrains.annotations.Nullable()
    java.lang.String rootKey) {
    }
    
    /**
     * Device name preference — shows current device model name by default.
     * Summary dynamically reflects the current value.
     */
    private final void setupDeviceNamePref() {
    }
    
    /**
     * Visibility mode preference — a dropdown list with three options:
     * "Everyone", "Your contacts", "Hidden".
     * Summary reflects the currently selected option.
     */
    private final void setupVisibilityPref() {
    }
    
    /**
     * Data usage preference — a toggle for Wi-Fi-only vs all networks.
     * Default: Wi-Fi only (to avoid unexpected mobile data usage).
     */
    private final void setupDataPref() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0004\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\b"}, d2 = {"Lcom/nearbyshare/ui/fragments/SettingsFragment$Companion;", "", "()V", "PREF_DEVICE_NAME", "", "PREF_SHOW_NOTIFICATION", "PREF_VISIBILITY_MODE", "PREF_WIFI_ONLY", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}