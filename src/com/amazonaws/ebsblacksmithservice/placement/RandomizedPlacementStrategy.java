package com.amazonaws.ebsblacksmithservice.placement;

import static com.amazonaws.ebsblacksmithservice.placement.PlacementStrategyDecider.registerPlacementStrategy;
import static com.amazonaws.ebsblacksmithservice.placement.PlacementStrategyType.RANDOM;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import com.amazonaws.ebsblacksmithservice.capacity.CapacityCache;
import com.amazonaws.ebsblacksmithservice.types.MetalDiskInternal;
import com.amazonaws.ebsblacksmithservice.types.MetalServerInternal;

/**
 * This placement strategy returns all metal servers and list of disk in a random order.
 */

@Slf4j
public class RandomizedPlacementStrategy implements PlacementStrategy {
    private final CapacityCache cache;

    public RandomizedPlacementStrategy(final CapacityCache cache) {
        this.cache = cache;
        registerPlacementStrategy(RANDOM, this);
    }


    /*
     * Not filtering servers to allow delete volume call to execute on all servers.
     */
    @Override
    public List<MetalServerInternal> placementServers(final PlacementOptions options) {
        var servers = cache.getMetalServers();
        Collections.shuffle(servers);
        return servers;
    }

    @Override
    public List<MetalDiskInternal> placementDisks(final PlacementOptions options) {
        log.info("Randomized placement strategy triggered");
        var disks = cache.getMetalDisks();
        Collections.shuffle(disks);
        return disks
            .stream()
            .filter(disk -> !TARGETED_ONLY_PLACEMENT_SERVER_ADDRESSES.contains(disk.getServerAddress()))
            .limit(options.getDiskResponseSizeRequested())
            .collect(Collectors.toList());
    }
}
