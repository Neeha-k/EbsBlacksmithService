package com.amazonaws.ebsblacksmithservice.placement;

import java.util.List;
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

    public Optional<List<String>> getTargetServerIpList() {
        return getTargetingOptions().flatMap(TargetingOptionsInternal::getServerIpList);
    }

    public boolean hasTargetingOptionForPlacement() {
        return getTargetServerIpList().isPresent();
    }
}
