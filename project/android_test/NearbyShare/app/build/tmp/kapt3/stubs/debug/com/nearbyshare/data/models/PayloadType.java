package com.nearbyshare.data.models;

import android.net.Uri;
import android.os.Parcelable;
import kotlinx.parcelize.Parcelize;

/**
 * Describes the kind of data encapsulated in a [SharePayload].
 * The sender and receiver use this to decide how to render a preview
 * and how to handle the incoming data once received.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0006\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/nearbyshare/data/models/PayloadType;", "", "(Ljava/lang/String;I)V", "FILE", "TEXT", "URL", "MULTIPLE", "app_debug"})
public enum PayloadType {
    /*public static final*/ FILE /* = new FILE() */,
    /*public static final*/ TEXT /* = new TEXT() */,
    /*public static final*/ URL /* = new URL() */,
    /*public static final*/ MULTIPLE /* = new MULTIPLE() */;
    
    PayloadType() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static kotlin.enums.EnumEntries<com.nearbyshare.data.models.PayloadType> getEntries() {
        return null;
    }
}