// =============================================================================
// FILE: AppModule.kt
// Package: com.nearbyshare.di
// =============================================================================
// INDEX OF CONTENTS:
//   1. AppModule — Hilt module providing singleton application-level bindings
//   2. Repository binding (interface → implementation)
//   3. ApplicationContext provision
//
// OBJECTIVE:
//   Hilt dependency injection module that wires together the concrete
//   implementations with their interface types.
//   By binding INearbyShareRepository → NearbyShareRepository here,
//   ViewModels that declare a constructor dependency on the interface
//   automatically receive the real singleton implementation at runtime,
//   while tests can swap in a fake without modifying production code.
// =============================================================================

package com.nearbyshare.di

import com.nearbyshare.data.repository.INearbyShareRepository
import com.nearbyshare.data.repository.NearbyShareRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module installed in [SingletonComponent] — lives for the entire
 * application lifecycle (created with the Application, destroyed with it).
 *
 * All @Binds and @Provides methods here produce singletons unless
 * explicitly scoped otherwise.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    /**
     * Binds the concrete [NearbyShareRepository] to its interface type.
     *
     * Any class that injects [INearbyShareRepository] will receive the
     * singleton [NearbyShareRepository] instance.
     * The @Singleton annotation ensures only one instance exists app-wide.
     */
    @Binds
    @Singleton
    abstract fun bindNearbyShareRepository(
        impl: NearbyShareRepository
    ): INearbyShareRepository
}
