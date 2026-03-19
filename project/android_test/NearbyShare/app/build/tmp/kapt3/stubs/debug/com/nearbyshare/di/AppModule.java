package com.nearbyshare.di;

import com.nearbyshare.data.repository.INearbyShareRepository;
import com.nearbyshare.data.repository.NearbyShareRepository;
import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

/**
 * Hilt module installed in [SingletonComponent] — lives for the entire
 * application lifecycle (created with the Application, destroyed with it).
 *
 * All @Binds and @Provides methods here produce singletons unless
 * explicitly scoped otherwise.
 */
@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\'\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\'\u00a8\u0006\u0007"}, d2 = {"Lcom/nearbyshare/di/AppModule;", "", "()V", "bindNearbyShareRepository", "Lcom/nearbyshare/data/repository/INearbyShareRepository;", "impl", "Lcom/nearbyshare/data/repository/NearbyShareRepository;", "app_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public abstract class AppModule {
    
    public AppModule() {
        super();
    }
    
    /**
     * Binds the concrete [NearbyShareRepository] to its interface type.
     *
     * Any class that injects [INearbyShareRepository] will receive the
     * singleton [NearbyShareRepository] instance.
     * The @Singleton annotation ensures only one instance exists app-wide.
     */
    @dagger.Binds()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public abstract com.nearbyshare.data.repository.INearbyShareRepository bindNearbyShareRepository(@org.jetbrains.annotations.NotNull()
    com.nearbyshare.data.repository.NearbyShareRepository impl);
}