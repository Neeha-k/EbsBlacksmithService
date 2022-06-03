package com.amazonaws.ebsblacksmithservice.placement;

import com.amazonaws.ebsblacksmithservice.types.MetalServerInternal;

import java.util.List;

public interface PlacementStrategy {
//   TODO modify this to return Metal Disk
    List<MetalServerInternal> place(PlacementOptions options);
}
