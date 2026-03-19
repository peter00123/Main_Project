package com.nearbyshare.data.models;

import android.os.Parcelable;
import kotlinx.parcelize.Parcelize;

/**
 * Full lifecycle states of a transfer session, used to drive
 * UI transitions (spinner → progress bar → success/error screen).
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u000b\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\bj\u0002\b\tj\u0002\b\nj\u0002\b\u000b\u00a8\u0006\f"}, d2 = {"Lcom/nearbyshare/data/models/TransferStatus;", "", "(Ljava/lang/String;I)V", "PENDING", "CONNECTING", "AWAITING_ACCEPT", "AWAITING_USER", "TRANSFERRING", "COMPLETED", "DECLINED", "CANCELLED", "FAILED", "app_debug"})
public enum TransferStatus {
    /*public static final*/ PENDING /* = new PENDING() */,
    /*public static final*/ CONNECTING /* = new CONNECTING() */,
    /*public static final*/ AWAITING_ACCEPT /* = new AWAITING_ACCEPT() */,
    /*public static final*/ AWAITING_USER /* = new AWAITING_USER() */,
    /*public static final*/ TRANSFERRING /* = new TRANSFERRING() */,
    /*public static final*/ COMPLETED /* = new COMPLETED() */,
    /*public static final*/ DECLINED /* = new DECLINED() */,
    /*public static final*/ CANCELLED /* = new CANCELLED() */,
    /*public static final*/ FAILED /* = new FAILED() */;
    
    TransferStatus() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.nearbyshare.data.models.TransferStatus> getEntries() {
        return null;
    }
}