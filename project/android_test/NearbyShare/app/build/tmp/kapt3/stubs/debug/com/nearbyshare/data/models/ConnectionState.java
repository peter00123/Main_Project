package com.nearbyshare.data.models;

import android.os.Parcelable;
import kotlinx.parcelize.Parcelize;

/**
 * Tracks the BLE / Wi-Fi connection lifecycle for a nearby peer.
 * The UI observes this state to show appropriate visual feedback
 * (spinner, checkmark, error icon, etc.).
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\n\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\bj\u0002\b\tj\u0002\b\n\u00a8\u0006\u000b"}, d2 = {"Lcom/nearbyshare/data/models/ConnectionState;", "", "(Ljava/lang/String;I)V", "DISCOVERED", "CONNECTING", "AWAITING_ACCEPT", "TRANSFERRING", "COMPLETED", "REJECTED", "FAILED", "DISCONNECTED", "app_debug"})
public enum ConnectionState {
    /*public static final*/ DISCOVERED /* = new DISCOVERED() */,
    /*public static final*/ CONNECTING /* = new CONNECTING() */,
    /*public static final*/ AWAITING_ACCEPT /* = new AWAITING_ACCEPT() */,
    /*public static final*/ TRANSFERRING /* = new TRANSFERRING() */,
    /*public static final*/ COMPLETED /* = new COMPLETED() */,
    /*public static final*/ REJECTED /* = new REJECTED() */,
    /*public static final*/ FAILED /* = new FAILED() */,
    /*public static final*/ DISCONNECTED /* = new DISCONNECTED() */;
    
    ConnectionState() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.nearbyshare.data.models.ConnectionState> getEntries() {
        return null;
    }
}