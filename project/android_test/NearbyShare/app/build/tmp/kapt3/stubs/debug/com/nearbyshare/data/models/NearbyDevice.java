package com.nearbyshare.data.models;

import android.os.Parcelable;
import kotlinx.parcelize.Parcelize;

/**
 * Core data model representing a single peer device discovered nearby.
 *
 * @property id              Unique identifier — typically the BLE MAC address or
 *                          a UUID generated from the device's Bluetooth name.
 * @property name            Human-readable device name (e.g. "Pixel 8 Pro").
 * @property deviceType      Form factor classification for icon selection.
 * @property signalStrength  RSSI value from BLE advertisement (-100 to 0 dBm).
 *                          Higher is closer. Used to sort the device list.
 * @property connectionState Current lifecycle state of the connection.
 * @property avatarInitials  1–2 character string derived from device name,
 *                          rendered inside the avatar circle when no image exists.
 * @property isVisible       Whether THIS device is advertising itself as
 *                          discoverable to others (toggled by the user).
 * @property transferProgress Transfer progress percentage (0–100), only
 *                            meaningful when state == TRANSFERRING.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u001a\n\u0002\u0010\u0000\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001BQ\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b\u0012\b\b\u0002\u0010\t\u001a\u00020\n\u0012\b\b\u0002\u0010\u000b\u001a\u00020\u0003\u0012\b\b\u0002\u0010\f\u001a\u00020\r\u0012\b\b\u0002\u0010\u000e\u001a\u00020\b\u00a2\u0006\u0002\u0010\u000fJ\t\u0010\u001c\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001d\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u001e\u001a\u00020\u0006H\u00c6\u0003J\t\u0010\u001f\u001a\u00020\bH\u00c6\u0003J\t\u0010 \u001a\u00020\nH\u00c6\u0003J\t\u0010!\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\"\u001a\u00020\rH\u00c6\u0003J\t\u0010#\u001a\u00020\bH\u00c6\u0003JY\u0010$\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\u00032\b\b\u0002\u0010\f\u001a\u00020\r2\b\b\u0002\u0010\u000e\u001a\u00020\bH\u00c6\u0001J\t\u0010%\u001a\u00020\bH\u00d6\u0001J\u0013\u0010&\u001a\u00020\r2\b\u0010\'\u001a\u0004\u0018\u00010(H\u00d6\u0003J\t\u0010)\u001a\u00020\bH\u00d6\u0001J\u0006\u0010*\u001a\u00020\u0003J\u0006\u0010+\u001a\u00020\u0003J\t\u0010,\u001a\u00020\u0003H\u00d6\u0001J\u0019\u0010-\u001a\u00020.2\u0006\u0010/\u001a\u0002002\u0006\u00101\u001a\u00020\bH\u00d6\u0001R\u0011\u0010\u000b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0011\u0010\t\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0011R\u0011\u0010\f\u001a\u00020\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\u0017R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0011R\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u0011\u0010\u000e\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001a\u00a8\u00062"}, d2 = {"Lcom/nearbyshare/data/models/NearbyDevice;", "Landroid/os/Parcelable;", "id", "", "name", "deviceType", "Lcom/nearbyshare/data/models/DeviceType;", "signalStrength", "", "connectionState", "Lcom/nearbyshare/data/models/ConnectionState;", "avatarInitials", "isVisible", "", "transferProgress", "(Ljava/lang/String;Ljava/lang/String;Lcom/nearbyshare/data/models/DeviceType;ILcom/nearbyshare/data/models/ConnectionState;Ljava/lang/String;ZI)V", "getAvatarInitials", "()Ljava/lang/String;", "getConnectionState", "()Lcom/nearbyshare/data/models/ConnectionState;", "getDeviceType", "()Lcom/nearbyshare/data/models/DeviceType;", "getId", "()Z", "getName", "getSignalStrength", "()I", "getTransferProgress", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "copy", "describeContents", "equals", "other", "", "hashCode", "resolvedInitials", "signalLabel", "toString", "writeToParcel", "", "parcel", "Landroid/os/Parcel;", "flags", "app_debug"})
@kotlinx.parcelize.Parcelize()
public final class NearbyDevice implements android.os.Parcelable {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String id = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String name = null;
    @org.jetbrains.annotations.NotNull()
    private final com.nearbyshare.data.models.DeviceType deviceType = null;
    private final int signalStrength = 0;
    @org.jetbrains.annotations.NotNull()
    private final com.nearbyshare.data.models.ConnectionState connectionState = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String avatarInitials = null;
    private final boolean isVisible = false;
    private final int transferProgress = 0;
    
    public NearbyDevice(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    com.nearbyshare.data.models.DeviceType deviceType, int signalStrength, @org.jetbrains.annotations.NotNull()
    com.nearbyshare.data.models.ConnectionState connectionState, @org.jetbrains.annotations.NotNull()
    java.lang.String avatarInitials, boolean isVisible, int transferProgress) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.nearbyshare.data.models.DeviceType getDeviceType() {
        return null;
    }
    
    public final int getSignalStrength() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.nearbyshare.data.models.ConnectionState getConnectionState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getAvatarInitials() {
        return null;
    }
    
    public final boolean isVisible() {
        return false;
    }
    
    public final int getTransferProgress() {
        return 0;
    }
    
    /**
     * Returns a human-readable signal quality label based on RSSI.
     * Nearby Share groups signal into three informal tiers.
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String signalLabel() {
        return null;
    }
    
    /**
     * Derives the initials shown inside the avatar circle.
     * Takes the first letter of each word in the device name (max 2 letters).
     * Example: "Pixel 8 Pro" → "PP"
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String resolvedInitials() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.nearbyshare.data.models.DeviceType component3() {
        return null;
    }
    
    public final int component4() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.nearbyshare.data.models.ConnectionState component5() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component6() {
        return null;
    }
    
    public final boolean component7() {
        return false;
    }
    
    public final int component8() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.nearbyshare.data.models.NearbyDevice copy(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    com.nearbyshare.data.models.DeviceType deviceType, int signalStrength, @org.jetbrains.annotations.NotNull()
    com.nearbyshare.data.models.ConnectionState connectionState, @org.jetbrains.annotations.NotNull()
    java.lang.String avatarInitials, boolean isVisible, int transferProgress) {
        return null;
    }
    
    @java.lang.Override()
    public int describeContents() {
        return 0;
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
    
    @java.lang.Override()
    public void writeToParcel(@org.jetbrains.annotations.NotNull()
    android.os.Parcel parcel, int flags) {
    }
}