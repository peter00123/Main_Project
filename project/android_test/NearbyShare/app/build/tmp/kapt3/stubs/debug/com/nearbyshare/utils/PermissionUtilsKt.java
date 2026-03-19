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

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\u001c\n\u0000\n\u0002\u0010\u000b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\u0018\u0002\n\u0000\u001a\u0012\u0010\u0000\u001a\u00020\u0001*\u00020\u00022\u0006\u0010\u0003\u001a\u00020\u0004\u001a\n\u0010\u0005\u001a\u00020\u0006*\u00020\u0007\u00a8\u0006\b"}, d2 = {"hasPermission", "", "Landroid/app/Activity;", "permission", "", "openAppSettings", "", "Landroid/content/Context;", "app_debug"})
public final class PermissionUtilsKt {
    
    /**
     * Extension on [Activity] to check a single permission quickly.
     *
     * Usage:
     *  if (hasPermission(Manifest.permission.BLUETOOTH_SCAN)) { ... }
     */
    public static final boolean hasPermission(@org.jetbrains.annotations.NotNull()
    android.app.Activity $this$hasPermission, @org.jetbrains.annotations.NotNull()
    java.lang.String permission) {
        return false;
    }
    
    /**
     * Opens the app's system settings page so the user can manually grant
     * permissions that were permanently denied (Don't ask again).
     *
     * Usage: context.openAppSettings()
     */
    public static final void openAppSettings(@org.jetbrains.annotations.NotNull()
    android.content.Context $this$openAppSettings) {
    }
}