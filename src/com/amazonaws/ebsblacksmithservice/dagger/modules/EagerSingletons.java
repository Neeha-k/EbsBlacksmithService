package com.amazonaws.ebsblacksmithservice.dagger.modules;

import javax.inject.Inject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.amazonaws.ebsblacksmithservice.placement.RandomizedPlacementStrategy;
import com.amazonaws.ebsblacksmithservice.placement.TargetingPlacementStrategy;

/**
 * Dagger doesn't support eager singletons, singletons are created lazily.
 * This class forces eager loading of any eager singletons required,
 * by calling `init` during service startup - {@link com.amazonaws.ebsblacksmithservice.dagger.EbsBlacksmithServiceManager#start}.
 * For any singleton that is to be eager loaded, inject it in this class.
 */

@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class EagerSingletons {

    private final RandomizedPlacementStrategy randomizedPlacementStrategy;
    private final TargetingPlacementStrategy targetingPlacementStrategy;

    public void init() {
        log.info("Eager loading singletons");
    }
}
