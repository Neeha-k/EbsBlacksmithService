package com.amazonaws.ebsblacksmithservice.placement;

import com.amazonaws.ebsblacksmithservice.capacity.CapacityCache;
import com.amazonaws.ebsblacksmithservice.types.MetalServerInternal;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This placement strategy returns a list of metal servers in a random order.
 *
 * A metal server with no capacity available will not be returned as a recommendation.
 */
public class RandomizedPlacementStrategy implements PlacementStrategy {
    CapacityCache cache;

    public RandomizedPlacementStrategy(CapacityCache cache) {
        this.cache = cache;
    }

    @Override
    public List<MetalServerInternal> place(PlacementOptions options) {
        var servers = cache.getMetalServers()
                .stream()
                .filter(server -> server.getAvailableDisks() != 0)
                .collect(Collectors.toList());

        Collections.shuffle(servers);

        return servers
                .stream()
                .limit(options.getResponseSizeRequested())
                .collect(Collectors.toList());
    }
}
