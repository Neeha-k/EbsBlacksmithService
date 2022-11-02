package com.amazonaws.ebsblacksmithservice.placement;

import com.amazonaws.ebsblacksmithservice.types.MetalDiskInternal;
import com.amazonaws.ebsblacksmithservice.types.MetalServerInternal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface PlacementStrategy {
    List<String> TARGETED_ONLY_PLACEMENT_SERVER_ADDRESSES = Collections.unmodifiableList((Arrays.asList("30.135.143.139:5684")));
    List<MetalServerInternal> placementServers(PlacementOptions options);
    List<MetalDiskInternal> placementDisks(PlacementOptions options);
}
