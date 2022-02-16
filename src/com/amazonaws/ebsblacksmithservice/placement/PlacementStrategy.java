package com.amazonaws.ebsblacksmithservice.placement;

import com.amazonaws.ebsblacksmithservice.types.MetalServerInternal;

import java.util.List;

public interface PlacementStrategy {
    List<MetalServerInternal> place(PlacementOptions options);
}
