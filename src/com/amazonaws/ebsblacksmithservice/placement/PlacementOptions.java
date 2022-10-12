package com.amazonaws.ebsblacksmithservice.placement;

import java.util.Optional;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlacementOptions {
    private int diskResponseSizeRequested;
    private TargetingOptionsInternal targetingOptions;

    public Optional<TargetingOptionsInternal> getTargetingOptions() {
        return Optional.ofNullable(this.targetingOptions);
    }

    public Optional<String> getTargetServerIp() {
        return getTargetingOptions().flatMap(TargetingOptionsInternal::getServerIp);
    }

    public boolean hasTargetingOptionForPlacement() {
        return getTargetServerIp().isPresent();
    }
}
