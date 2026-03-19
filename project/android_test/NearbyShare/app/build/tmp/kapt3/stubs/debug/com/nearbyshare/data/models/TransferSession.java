package com.nearbyshare.data.models;

import android.os.Parcelable;
import kotlinx.parcelize.Parcelize;

/**
 * Represents one complete file-transfer session.
 *
 * @property sessionId      Unique identifier for this transfer session.
 * @property remoteDevice   The peer device on the other side of the transfer.
 * @property payload        The content being transferred (file, text, URL).
 * @property direction      Whether we are the sender or receiver.
 * @property status         Current lifecycle state.
 * @property bytesTransferred How many bytes have been sent/received so far.
 * @property totalBytes     Total bytes to transfer (mirrors payload.totalBytes).
 * @property speedBytesPerSec Current transfer speed, updated ~1 Hz.
 * @property startedAtMs    System.currentTimeMillis() when transfer started.
 * @property completedAtMs  System.currentTimeMillis() when transfer completed (or 0).
 * @property errorMessage   Human-readable error description if status == FAILED.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u000e\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0019\n\u0002\u0010\u0000\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001Bk\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\b\b\u0002\u0010\n\u001a\u00020\u000b\u0012\b\b\u0002\u0010\f\u001a\u00020\r\u0012\b\b\u0002\u0010\u000e\u001a\u00020\r\u0012\b\b\u0002\u0010\u000f\u001a\u00020\r\u0012\b\b\u0002\u0010\u0010\u001a\u00020\r\u0012\b\b\u0002\u0010\u0011\u001a\u00020\r\u0012\b\b\u0002\u0010\u0012\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0013J\t\u0010,\u001a\u00020\u0003H\u00c6\u0003J\t\u0010-\u001a\u00020\rH\u00c6\u0003J\t\u0010.\u001a\u00020\u0003H\u00c6\u0003J\t\u0010/\u001a\u00020\u0005H\u00c6\u0003J\t\u00100\u001a\u00020\u0007H\u00c6\u0003J\t\u00101\u001a\u00020\tH\u00c6\u0003J\t\u00102\u001a\u00020\u000bH\u00c6\u0003J\t\u00103\u001a\u00020\rH\u00c6\u0003J\t\u00104\u001a\u00020\rH\u00c6\u0003J\t\u00105\u001a\u00020\rH\u00c6\u0003J\t\u00106\u001a\u00020\rH\u00c6\u0003Jw\u00107\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\f\u001a\u00020\r2\b\b\u0002\u0010\u000e\u001a\u00020\r2\b\b\u0002\u0010\u000f\u001a\u00020\r2\b\b\u0002\u0010\u0010\u001a\u00020\r2\b\b\u0002\u0010\u0011\u001a\u00020\r2\b\b\u0002\u0010\u0012\u001a\u00020\u0003H\u00c6\u0001J\t\u00108\u001a\u00020!H\u00d6\u0001J\u0013\u00109\u001a\u00020\u001c2\b\u0010:\u001a\u0004\u0018\u00010;H\u00d6\u0003J\u0006\u0010<\u001a\u00020\u0003J\u0006\u0010=\u001a\u00020\u0003J\t\u0010>\u001a\u00020!H\u00d6\u0001J\t\u0010?\u001a\u00020\u0003H\u00d6\u0001J\u0019\u0010@\u001a\u00020A2\u0006\u0010B\u001a\u00020C2\u0006\u0010D\u001a\u00020!H\u00d6\u0001R\u0011\u0010\f\u001a\u00020\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015R\u0011\u0010\u0011\u001a\u00020\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0015R\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u0011\u0010\u0012\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u0011\u0010\u001b\u001a\u00020\u001c8F\u00a2\u0006\u0006\u001a\u0004\b\u001b\u0010\u001dR\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001fR\u0011\u0010 \u001a\u00020!8F\u00a2\u0006\u0006\u001a\u0004\b\"\u0010#R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010%R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\u001aR\u0011\u0010\u000f\u001a\u00020\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010\u0015R\u0011\u0010\u0010\u001a\u00020\r\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010\u0015R\u0011\u0010\n\u001a\u00020\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010*R\u0011\u0010\u000e\u001a\u00020\r\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010\u0015\u00a8\u0006E"}, d2 = {"Lcom/nearbyshare/data/models/TransferSession;", "Landroid/os/Parcelable;", "sessionId", "", "remoteDevice", "Lcom/nearbyshare/data/models/NearbyDevice;", "payload", "Lcom/nearbyshare/data/models/SharePayload;", "direction", "Lcom/nearbyshare/data/models/TransferDirection;", "status", "Lcom/nearbyshare/data/models/TransferStatus;", "bytesTransferred", "", "totalBytes", "speedBytesPerSec", "startedAtMs", "completedAtMs", "errorMessage", "(Ljava/lang/String;Lcom/nearbyshare/data/models/NearbyDevice;Lcom/nearbyshare/data/models/SharePayload;Lcom/nearbyshare/data/models/TransferDirection;Lcom/nearbyshare/data/models/TransferStatus;JJJJJLjava/lang/String;)V", "getBytesTransferred", "()J", "getCompletedAtMs", "getDirection", "()Lcom/nearbyshare/data/models/TransferDirection;", "getErrorMessage", "()Ljava/lang/String;", "isTerminal", "", "()Z", "getPayload", "()Lcom/nearbyshare/data/models/SharePayload;", "progressPercent", "", "getProgressPercent", "()I", "getRemoteDevice", "()Lcom/nearbyshare/data/models/NearbyDevice;", "getSessionId", "getSpeedBytesPerSec", "getStartedAtMs", "getStatus", "()Lcom/nearbyshare/data/models/TransferStatus;", "getTotalBytes", "component1", "component10", "component11", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "describeContents", "equals", "other", "", "estimatedTimeRemaining", "formattedSpeed", "hashCode", "toString", "writeToParcel", "", "parcel", "Landroid/os/Parcel;", "flags", "app_debug"})
@kotlinx.parcelize.Parcelize()
public final class TransferSession implements android.os.Parcelable {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String sessionId = null;
    @org.jetbrains.annotations.NotNull()
    private final com.nearbyshare.data.models.NearbyDevice remoteDevice = null;
    @org.jetbrains.annotations.NotNull()
    private final com.nearbyshare.data.models.SharePayload payload = null;
    @org.jetbrains.annotations.NotNull()
    private final com.nearbyshare.data.models.TransferDirection direction = null;
    @org.jetbrains.annotations.NotNull()
    private final com.nearbyshare.data.models.TransferStatus status = null;
    private final long bytesTransferred = 0L;
    private final long totalBytes = 0L;
    private final long speedBytesPerSec = 0L;
    private final long startedAtMs = 0L;
    private final long completedAtMs = 0L;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String errorMessage = null;
    
    public TransferSession(@org.jetbrains.annotations.NotNull()
    java.lang.String sessionId, @org.jetbrains.annotations.NotNull()
    com.nearbyshare.data.models.NearbyDevice remoteDevice, @org.jetbrains.annotations.NotNull()
    com.nearbyshare.data.models.SharePayload payload, @org.jetbrains.annotations.NotNull()
    com.nearbyshare.data.models.TransferDirection direction, @org.jetbrains.annotations.NotNull()
    com.nearbyshare.data.models.TransferStatus status, long bytesTransferred, long totalBytes, long speedBytesPerSec, long startedAtMs, long completedAtMs, @org.jetbrains.annotations.NotNull()
    java.lang.String errorMessage) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getSessionId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.nearbyshare.data.models.NearbyDevice getRemoteDevice() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.nearbyshare.data.models.SharePayload getPayload() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.nearbyshare.data.models.TransferDirection getDirection() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.nearbyshare.data.models.TransferStatus getStatus() {
        return null;
    }
    
    public final long getBytesTransferred() {
        return 0L;
    }
    
    public final long getTotalBytes() {
        return 0L;
    }
    
    public final long getSpeedBytesPerSec() {
        return 0L;
    }
    
    public final long getStartedAtMs() {
        return 0L;
    }
    
    public final long getCompletedAtMs() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getErrorMessage() {
        return null;
    }
    
    public final int getProgressPercent() {
        return 0;
    }
    
    /**
     * Formats the current transfer speed as a human-readable string.
     * Example output: "2.4 MB/s", "850 KB/s"
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String formattedSpeed() {
        return null;
    }
    
    /**
     * Estimates time remaining based on current speed and bytes left.
     * Returns an empty string if speed is unknown or transfer is done.
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String estimatedTimeRemaining() {
        return null;
    }
    
    public final boolean isTerminal() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    public final long component10() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component11() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.nearbyshare.data.models.NearbyDevice component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.nearbyshare.data.models.SharePayload component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.nearbyshare.data.models.TransferDirection component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.nearbyshare.data.models.TransferStatus component5() {
        return null;
    }
    
    public final long component6() {
        return 0L;
    }
    
    public final long component7() {
        return 0L;
    }
    
    public final long component8() {
        return 0L;
    }
    
    public final long component9() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.nearbyshare.data.models.TransferSession copy(@org.jetbrains.annotations.NotNull()
    java.lang.String sessionId, @org.jetbrains.annotations.NotNull()
    com.nearbyshare.data.models.NearbyDevice remoteDevice, @org.jetbrains.annotations.NotNull()
    com.nearbyshare.data.models.SharePayload payload, @org.jetbrains.annotations.NotNull()
    com.nearbyshare.data.models.TransferDirection direction, @org.jetbrains.annotations.NotNull()
    com.nearbyshare.data.models.TransferStatus status, long bytesTransferred, long totalBytes, long speedBytesPerSec, long startedAtMs, long completedAtMs, @org.jetbrains.annotations.NotNull()
    java.lang.String errorMessage) {
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