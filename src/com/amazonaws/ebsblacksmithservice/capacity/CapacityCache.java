package com.amazonaws.ebsblacksmithservice.capacity;

import com.amazonaws.ebsblacksmithservice.types.MetalServerInternal;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * CapacityCache holds an in-memory view of the Metal Servers to be used for placement.
 * This cache uses the CapacityProvider to replace its data with fresh data.
 *
 * For now, this cache maintains a simple List of metal servers, but future improvements can be added to bake in
 * data center topology to support more optimized queries instead of iterating through the list
 */
@Slf4j
public class CapacityCache {
    CapacityProvider capacityProvider;

    List<MetalServerInternal> servers;

    public CapacityCache(CapacityProvider capacityProvider) {
        this.capacityProvider = capacityProvider;
        update();
    }

    public List<MetalServerInternal> getMetalServers() {
        return this.servers;
    }

    public void update() {
        log.info("Refreshing the capacity cache");
        servers = capacityProvider.loadServerData()
            .stream()
            .filter(server -> isNonPublicRouteableIp(server.getIpAddress()))
            .collect(Collectors.toList());
        log.info(String.format("Loaded %s MetalServer entries into the cache", servers.size()));
    }

    private boolean isNonPublicRouteableIp(String ip) {
        return ip.startsWith("127.");
    }
}
