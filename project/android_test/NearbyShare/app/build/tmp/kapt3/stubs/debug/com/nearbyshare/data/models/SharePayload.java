package com.nearbyshare.data.models;

import android.net.Uri;
import android.os.Parcelable;
import kotlinx.parcelize.Parcelize;

/**
 * Wraps the data the user wants to transmit to a nearby peer.
 *
 * @property type         The kind of content (file, text, URL, multiple files).
 * @property uris         List of content URIs for file payloads.
 *                       For TEXT/URL payloads this list is empty.
 * @property text         Raw text or URL string for TEXT/URL payloads.
 * @property displayName  Human-readable name shown in transfer previews.
 *                       For files this is the filename; for text a truncated snippet.
 * @property mimeType     MIME type string (e.g. "image/jpeg", "application/pdf").
 *                       Used to show the correct file-type icon.
 * @property totalBytes   Total size of all payloads combined, in bytes.
 *                       Displayed as "3.2 MB" in the transfer preview card.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0013\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0087\b\u0018\u0000 -2\u00020\u0001:\u0001-BE\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u0012\b\b\u0002\u0010\u0007\u001a\u00020\b\u0012\b\b\u0002\u0010\t\u001a\u00020\b\u0012\b\b\u0002\u0010\n\u001a\u00020\b\u0012\b\b\u0002\u0010\u000b\u001a\u00020\f\u00a2\u0006\u0002\u0010\rJ\t\u0010\u0018\u001a\u00020\u0003H\u00c6\u0003J\u000f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005H\u00c6\u0003J\t\u0010\u001a\u001a\u00020\bH\u00c6\u0003J\t\u0010\u001b\u001a\u00020\bH\u00c6\u0003J\t\u0010\u001c\u001a\u00020\bH\u00c6\u0003J\t\u0010\u001d\u001a\u00020\fH\u00c6\u0003JK\u0010\u001e\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\u000e\b\u0002\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\b2\b\b\u0002\u0010\n\u001a\u00020\b2\b\b\u0002\u0010\u000b\u001a\u00020\fH\u00c6\u0001J\t\u0010\u001f\u001a\u00020 H\u00d6\u0001J\u0013\u0010!\u001a\u00020\"2\b\u0010#\u001a\u0004\u0018\u00010$H\u00d6\u0003J\u0006\u0010%\u001a\u00020\bJ\t\u0010&\u001a\u00020 H\u00d6\u0001J\t\u0010\'\u001a\u00020\bH\u00d6\u0001J\u0019\u0010(\u001a\u00020)2\u0006\u0010*\u001a\u00020+2\u0006\u0010,\u001a\u00020 H\u00d6\u0001R\u0011\u0010\t\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0011\u0010\n\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u000fR\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u000fR\u0011\u0010\u000b\u001a\u00020\f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0017\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017\u00a8\u0006."}, d2 = {"Lcom/nearbyshare/data/models/SharePayload;", "Landroid/os/Parcelable;", "type", "Lcom/nearbyshare/data/models/PayloadType;", "uris", "", "Landroid/net/Uri;", "text", "", "displayName", "mimeType", "totalBytes", "", "(Lcom/nearbyshare/data/models/PayloadType;Ljava/util/List;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;J)V", "getDisplayName", "()Ljava/lang/String;", "getMimeType", "getText", "getTotalBytes", "()J", "getType", "()Lcom/nearbyshare/data/models/PayloadType;", "getUris", "()Ljava/util/List;", "component1", "component2", "component3", "component4", "component5", "component6", "copy", "describeContents", "", "equals", "", "other", "", "formattedSize", "hashCode", "toString", "writeToParcel", "", "parcel", "Landroid/os/Parcel;", "flags", "Companion", "app_debug"})
@kotlinx.parcelize.Parcelize()
public final class SharePayload implements android.os.Parcelable {
    @org.jetbrains.annotations.NotNull()
    private final com.nearbyshare.data.models.PayloadType type = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<android.net.Uri> uris = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String text = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String displayName = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String mimeType = null;
    private final long totalBytes = 0L;
    @org.jetbrains.annotations.NotNull()
    public static final com.nearbyshare.data.models.SharePayload.Companion Companion = null;
    
    public SharePayload(@org.jetbrains.annotations.NotNull()
    com.nearbyshare.data.models.PayloadType type, @org.jetbrains.annotations.NotNull()
    java.util.List<? extends android.net.Uri> uris, @org.jetbrains.annotations.NotNull()
    java.lang.String text, @org.jetbrains.annotations.NotNull()
    java.lang.String displayName, @org.jetbrains.annotations.NotNull()
    java.lang.String mimeType, long totalBytes) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.nearbyshare.data.models.PayloadType getType() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<android.net.Uri> getUris() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getText() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getDisplayName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getMimeType() {
        return null;
    }
    
    public final long getTotalBytes() {
        return 0L;
    }
    
    /**
     * Formats [totalBytes] as a human-readable string (KB / MB / GB).
     * Returns an empty string for non-file payloads.
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String formattedSize() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.nearbyshare.data.models.PayloadType component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<android.net.Uri> component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component5() {
        return null;
    }
    
    public final long component6() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.nearbyshare.data.models.SharePayload copy(@org.jetbrains.annotations.NotNull()
    com.nearbyshare.data.models.PayloadType type, @org.jetbrains.annotations.NotNull()
    java.util.List<? extends android.net.Uri> uris, @org.jetbrains.annotations.NotNull()
    java.lang.String text, @org.jetbrains.annotations.NotNull()
    java.lang.String displayName, @org.jetbrains.annotations.NotNull()
    java.lang.String mimeType, long totalBytes) {
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\u0006\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J*\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\b2\b\b\u0002\u0010\n\u001a\u00020\u000bJ\u001e\u0010\f\u001a\u00020\u00042\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00060\u000e2\b\b\u0002\u0010\u000f\u001a\u00020\u000bJ\u000e\u0010\u0010\u001a\u00020\u00042\u0006\u0010\u0011\u001a\u00020\bJ\u000e\u0010\u0012\u001a\u00020\u00042\u0006\u0010\u0013\u001a\u00020\b\u00a8\u0006\u0014"}, d2 = {"Lcom/nearbyshare/data/models/SharePayload$Companion;", "", "()V", "fromFile", "Lcom/nearbyshare/data/models/SharePayload;", "uri", "Landroid/net/Uri;", "displayName", "", "mimeType", "sizeBytes", "", "fromMultipleFiles", "uris", "", "totalSize", "fromText", "text", "fromUrl", "url", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        /**
         * Factory: creates a payload for a single file URI.
         *
         * @param uri          Content URI pointing to the file.
         * @param displayName  Filename to show in the UI.
         * @param mimeType     MIME type of the file.
         * @param sizeBytes    File size in bytes.
         */
        @org.jetbrains.annotations.NotNull()
        public final com.nearbyshare.data.models.SharePayload fromFile(@org.jetbrains.annotations.NotNull()
        android.net.Uri uri, @org.jetbrains.annotations.NotNull()
        java.lang.String displayName, @org.jetbrains.annotations.NotNull()
        java.lang.String mimeType, long sizeBytes) {
            return null;
        }
        
        /**
         * Factory: creates a payload for multiple file URIs.
         *
         * @param uris      List of content URIs.
         * @param totalSize Combined byte size of all files.
         */
        @org.jetbrains.annotations.NotNull()
        public final com.nearbyshare.data.models.SharePayload fromMultipleFiles(@org.jetbrains.annotations.NotNull()
        java.util.List<? extends android.net.Uri> uris, long totalSize) {
            return null;
        }
        
        /**
         * Factory: creates a payload wrapping a plain-text string.
         *
         * @param text The text to share (e.g. copied clipboard content).
         */
        @org.jetbrains.annotations.NotNull()
        public final com.nearbyshare.data.models.SharePayload fromText(@org.jetbrains.annotations.NotNull()
        java.lang.String text) {
            return null;
        }
        
        /**
         * Factory: creates a payload wrapping a URL.
         *
         * @param url The full URL string to share.
         */
        @org.jetbrains.annotations.NotNull()
        public final com.nearbyshare.data.models.SharePayload fromUrl(@org.jetbrains.annotations.NotNull()
        java.lang.String url) {
            return null;
        }
    }
}