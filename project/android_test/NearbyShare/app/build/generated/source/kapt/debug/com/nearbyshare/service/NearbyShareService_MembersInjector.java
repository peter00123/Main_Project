package com.nearbyshare.service;

import com.nearbyshare.data.repository.INearbyShareRepository;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class NearbyShareService_MembersInjector implements MembersInjector<NearbyShareService> {
  private final Provider<INearbyShareRepository> repositoryProvider;

  public NearbyShareService_MembersInjector(Provider<INearbyShareRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  public static MembersInjector<NearbyShareService> create(
      Provider<INearbyShareRepository> repositoryProvider) {
    return new NearbyShareService_MembersInjector(repositoryProvider);
  }

  @Override
  public void injectMembers(NearbyShareService instance) {
    injectRepository(instance, repositoryProvider.get());
  }

  @InjectedFieldSignature("com.nearbyshare.service.NearbyShareService.repository")
  public static void injectRepository(NearbyShareService instance,
      INearbyShareRepository repository) {
    instance.repository = repository;
  }
}
