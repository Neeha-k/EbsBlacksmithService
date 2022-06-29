package com.amazonaws.ebsblacksmithservice.dagger.modules;

import com.amazonaws.ebsblacksmithservice.capacity.CapacityCache;
import com.amazonaws.ebsblacksmithservice.capacity.CapacityProvider;
import com.amazonaws.ebsblacksmithservice.capacity.FileReaderCapacityProvider;
import com.amazonaws.ebsblacksmithservice.placement.RandomizedPlacementStrategy;
import com.amazonaws.ebsblacksmithservice.placement.PlacementStrategy;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.inject.Singleton;

@Module
public class PlacementModule {
    @Provides
    @Singleton
    @Named("FileCapacityProvider")
    static CapacityProvider provideFileCapacityProvider(
            @Named("Root") String root,
            @Named("blacksmith.placementDataFile") String placementDataFileLocation) {
        return new FileReaderCapacityProvider(root +
                placementDataFileLocation);
    }

    @Provides
    @Singleton
    static CapacityCache provideCapacityCache(@Named("FileCapacityProvider") CapacityProvider capacityProvider) {
        return new CapacityCache(capacityProvider);
    }

    @Provides
    @Singleton
    static PlacementStrategy provideRandomizedPlacementStrategy(CapacityCache capacityCache) {
        return new RandomizedPlacementStrategy(capacityCache);
    }
}
