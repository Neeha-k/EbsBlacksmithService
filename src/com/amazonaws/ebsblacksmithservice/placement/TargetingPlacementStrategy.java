package com.amazonaws.ebsblacksmithservice.placement;

import static com.amazonaws.ebsblacksmithservice.placement.PlacementStrategyDecider.registerPlacementStrategy;
import static com.amazonaws.ebsblacksmithservice.placement.PlacementStrategyType.TARGETING;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.amazonaws.ebsblacksmithservice.capacity.CapacityCache;
import com.amazonaws.ebsblacksmithservice.types.MetalDiskInternal;
import com.amazonaws.ebsblacksmithservice.types.MetalServerInternal;

/**
 * This placement strategy returns all targeted servers and list of disk in the targeted server. In future this may
 * evolve to handle server pool, disk etc
 */
public class TargetingPlacementStrategy implements PlacementStrategy {
    private final CapacityCache cache;

    public TargetingPlacementStrategy(final CapacityCache cache) {
        this.cache = cache;
        registerPlacementStrategy(TARGETING, this);
    }

    @Override
    public List<MetalServerInternal> placementServers(final PlacementOptions options) {
        var servers = cache.getMetalServers();

        final List<MetalServerInternal> targetedServers =
            options.getTargetServerIpList()
                .map(targetServerIpList -> servers
                    .stream()
                    .filter(server -> targetServerIpList.contains(server.getIp()))
                    .collect(Collectors.toList()))
                .orElse(servers);
        Collections.shuffle(targetedServers);
        return targetedServers;
    }

    @Override
    public List<MetalDiskInternal> placementDisks(final PlacementOptions options) {
        var disks = cache.getMetalDisks();

        final List<MetalDiskInternal> targetedDisks =
            options.getTargetServerIpList()
                .map(targetServerIpList -> disks
                    .stream()
                    .filter(disk -> targetServerIpList.contains(disk.getDiskServerIp())))
                .orElse(disks.stream())
                .limit(options.getDiskResponseSizeRequested())
                .collect(Collectors.toList());
        Collections.shuffle(targetedDisks);
        return targetedDisks;
    }
}
