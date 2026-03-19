package com.nearbyshare.data.models;

import android.os.Parcelable;
import kotlinx.parcelize.Parcelize;

/**
 * Classifies discovered devices by their physical form factor.
 * Used to pick the correct device icon in the UI.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0007\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007\u00a8\u0006\b"}, d2 = {"Lcom/nearbyshare/data/models/DeviceType;", "", "(Ljava/lang/String;I)V", "PHONE", "TABLET", "LAPTOP", "WATCH", "UNKNOWN", "app_debug"})
public enum DeviceType {
    /*public static final*/ PHONE /* = new PHONE() */,
    /*public static final*/ TABLET /* = new TABLET() */,
    /*public static final*/ LAPTOP /* = new LAPTOP() */,
    /*public static final*/ WATCH /* = new WATCH() */,
    /*public static final*/ UNKNOWN /* = new UNKNOWN() */;
    
    DeviceType() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.nearbyshare.data.models.DeviceType> getEntries() {
        return null;
    }
}