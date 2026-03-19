package com.nearbyshare.ui.viewmodels;

import com.nearbyshare.data.repository.INearbyShareRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class TransferViewModel_Factory implements Factory<TransferViewModel> {
  private final Provider<INearbyShareRepository> repositoryProvider;

  public TransferViewModel_Factory(Provider<INearbyShareRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public TransferViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static TransferViewModel_Factory create(
      Provider<INearbyShareRepository> repositoryProvider) {
    return new TransferViewModel_Factory(repositoryProvider);
  }

  public static TransferViewModel newInstance(INearbyShareRepository repository) {
    return new TransferViewModel(repository);
  }
}
