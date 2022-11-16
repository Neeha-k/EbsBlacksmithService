package com.amazonaws.ebsblacksmithservice.placement;

import java.util.List;

import com.amazonaws.ebsblacksmithservice.types.MetalDiskInternal;
import com.amazonaws.ebsblacksmithservice.types.MetalServerInternal;

public interface PlacementStrategy {
    List<String> TARGETED_ONLY_PLACEMENT_SERVER_ADDRESSES = List.of(
        "30.133.7.112:5684", "30.135.143.141:5684", 
        "30.135.143.138:5684", "30.133.7.104:5684",
        "30.133.7.86:5684"
    );

    List<MetalServerInternal> placementServers(PlacementOptions options);

    List<MetalDiskInternal> placementDisks(PlacementOptions options);
}
