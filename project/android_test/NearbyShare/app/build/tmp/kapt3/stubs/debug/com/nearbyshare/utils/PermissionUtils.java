package com.nearbyshare.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.core.content.ContextCompat;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u001c\u0010\u0007\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\f\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\tJ\f\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\n0\t\u00a8\u0006\f"}, d2 = {"Lcom/nearbyshare/utils/PermissionUtils;", "", "()V", "areAllRequiredPermissionsGranted", "", "context", "Landroid/content/Context;", "arePermissionsGranted", "permissions", "", "", "getRequiredPermissions", "app_debug"})
public final class PermissionUtils {
    @org.jetbrains.annotations.NotNull()
    public static final com.nearbyshare.utils.PermissionUtils INSTANCE = null;
    
    private PermissionUtils() {
        super();
    }
    
    /**
     * Returns the set of permissions required for NearbyShare on the
     * current device's API level.
     *
     * This handles the three main eras of Android permission changes:
     *
     *  API 23–28 (Android 6–8): Classic Bluetooth + coarse/fine location
     *  API 29–30 (Android 10–11): Fine location mandatory for BLE scanning
     *  API 31–32 (Android 12):   New BLUETOOTH_SCAN/CONNECT/ADVERTISE model
     *  API 33+   (Android 13):   NEARBY_WIFI_DEVICES + READ_MEDIA_*
     *
     * @return List of permission strings to pass to the permission launcher.
     */
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<java.lang.String> getRequiredPermissions() {
        return null;
    }
    
    /**
     * Checks whether all permissions in [permissions] are currently granted.
     *
     * @param context     Any context (Activity or Application).
     * @param permissions List of Manifest.permission.* strings to check.
     * @return true if every permission is PERMISSION_GRANTED, false otherwise.
     */
    public final boolean arePermissionsGranted(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> permissions) {
        return false;
    }
    
    /**
     * Convenience: checks whether all REQUIRED permissions for NearbyShare
     * are granted on the current device.
     */
    public final boolean areAllRequiredPermissionsGranted(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
}