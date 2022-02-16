package com.amazonaws.ebsblacksmithservice.dagger.modules;

import com.amazonaws.ebsblacksmithservice.capacity.CapacityCache;
import com.amazonaws.ebsblacksmithservice.capacity.CapacityProvider;
import com.amazonaws.ebsblacksmithservice.capacity.FileReaderCapacityProvider;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.inject.Singleton;

@Module
public class PlacementModule {
    @Provides
    @Singleton
    @Named("FileCapacityProvider")
    static CapacityProvider provideFileCapacityProvider() {
        return new FileReaderCapacityProvider("MockPlacementData.json");
    }

    @Provides
    @Singleton
    static CapacityCache provideCapacityCache(@Named("FileCapacityProvider") CapacityProvider capacityProvider) {
        return new CapacityCache(capacityProvider);
    }
}
