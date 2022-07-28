package com.amazonaws.ebsblacksmithservice.placement;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;

import com.amazonaws.ebsblacksmithservice.capacity.CapacityCache;
import com.amazonaws.ebsblacksmithservice.types.MetalDiskInternal;
import com.amazonaws.ebsblacksmithservice.types.MetalServerInternal;

/**
 * This placement strategy returns all metal servers and list of disk in a random order.
 */
@AllArgsConstructor
public class RandomizedPlacementStrategy implements PlacementStrategy {
    private final CapacityCache cache;

    @Override
    public List<MetalServerInternal> getAllMetalServers() {
        var servers = cache.getMetalServers();
        Collections.shuffle(servers);
        return servers;
    }

    @Override
    public List<MetalDiskInternal> placementDisks(final PlacementOptions options) {
        var disks = cache.getMetalDisks();
        Collections.shuffle(disks);
        return disks
            .stream()
            .limit(options.getDiskResponseSizeRequested())
            .collect(Collectors.toList());
    }
}
