package com.nearbyshare.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class NearbyShareRepository_Factory implements Factory<NearbyShareRepository> {
  @Override
  public NearbyShareRepository get() {
    return newInstance();
  }

  public static NearbyShareRepository_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static NearbyShareRepository newInstance() {
    return new NearbyShareRepository();
  }

  private static final class InstanceHolder {
    private static final NearbyShareRepository_Factory INSTANCE = new NearbyShareRepository_Factory();
  }
}
