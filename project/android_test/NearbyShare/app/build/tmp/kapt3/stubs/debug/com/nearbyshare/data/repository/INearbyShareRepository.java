package com.nearbyshare.data.repository;

import com.nearbyshare.data.models.*;
import kotlinx.coroutines.*;
import kotlinx.coroutines.flow.*;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Contract that any data source backing the NearbyShare features must satisfy.
 * Using an interface allows unit tests to inject a fake implementation.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0007\bf\u0018\u00002\u00020\u0001J\u0016\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0010H\u00a6@\u00a2\u0006\u0002\u0010\u0011J\u0016\u0010\u0012\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0010H\u00a6@\u00a2\u0006\u0002\u0010\u0011J\u0016\u0010\u0013\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0010H\u00a6@\u00a2\u0006\u0002\u0010\u0011J\u001e\u0010\u0014\u001a\u00020\u00102\u0006\u0010\u0015\u001a\u00020\u000b2\u0006\u0010\u0016\u001a\u00020\u0017H\u00a6@\u00a2\u0006\u0002\u0010\u0018J\u0010\u0010\u0019\u001a\u00020\u000e2\u0006\u0010\u001a\u001a\u00020\bH&J\u000e\u0010\u001b\u001a\u00020\u000eH\u00a6@\u00a2\u0006\u0002\u0010\u001cJ\u000e\u0010\u001d\u001a\u00020\u000eH\u00a6@\u00a2\u0006\u0002\u0010\u001cR\u001a\u0010\u0002\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00040\u0003X\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0005\u0010\u0006R\u0018\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\b0\u0003X\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0007\u0010\u0006R\u001e\u0010\t\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\n0\u0003X\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\f\u0010\u0006\u00a8\u0006\u001e"}, d2 = {"Lcom/nearbyshare/data/repository/INearbyShareRepository;", "", "activeSession", "Lkotlinx/coroutines/flow/StateFlow;", "Lcom/nearbyshare/data/models/TransferSession;", "getActiveSession", "()Lkotlinx/coroutines/flow/StateFlow;", "isDeviceVisible", "", "nearbyDevices", "", "Lcom/nearbyshare/data/models/NearbyDevice;", "getNearbyDevices", "acceptTransfer", "", "sessionId", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "cancelTransfer", "declineTransfer", "sendTo", "targetDevice", "payload", "Lcom/nearbyshare/data/models/SharePayload;", "(Lcom/nearbyshare/data/models/NearbyDevice;Lcom/nearbyshare/data/models/SharePayload;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "setDeviceVisible", "visible", "startDiscovery", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "stopDiscovery", "app_debug"})
public abstract interface INearbyShareRepository {
    
    /**
     * Flow emitting the current list of discovered nearby devices.
     */
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.StateFlow<java.util.List<com.nearbyshare.data.models.NearbyDevice>> getNearbyDevices();
    
    /**
     * Flow emitting the active/most-recent transfer session, or null.
     */
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.StateFlow<com.nearbyshare.data.models.TransferSession> getActiveSession();
    
    /**
     * Flow emitting whether THIS device is currently advertising itself.
     */
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isDeviceVisible();
    
    /**
     * Starts scanning for nearby BLE devices.
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object startDiscovery(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * Stops BLE scanning.
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object stopDiscovery(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * Toggle whether this device is visible/discoverable to others.
     */
    public abstract void setDeviceVisible(boolean visible);
    
    /**
     * Initiates a transfer to [targetDevice] with the given [payload].
     * @return The session ID created for this transfer.
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object sendTo(@org.jetbrains.annotations.NotNull()
    com.nearbyshare.data.models.NearbyDevice targetDevice, @org.jetbrains.annotations.NotNull()
    com.nearbyshare.data.models.SharePayload payload, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion);
    
    /**
     * Accept an incoming transfer request by [sessionId].
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object acceptTransfer(@org.jetbrains.annotations.NotNull()
    java.lang.String sessionId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * Decline an incoming transfer request by [sessionId].
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object declineTransfer(@org.jetbrains.annotations.NotNull()
    java.lang.String sessionId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    /**
     * Cancel an ongoing transfer by [sessionId].
     */
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object cancelTransfer(@org.jetbrains.annotations.NotNull()
    java.lang.String sessionId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}