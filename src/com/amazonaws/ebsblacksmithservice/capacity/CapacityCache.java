package com.amazonaws.ebsblacksmithservice.capacity;

import com.amazonaws.ebsblacksmithservice.types.MetalServerInternal;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import java.util.concurrent.TimeUnit;
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

    private static final Duration INITIAL_DELAY = Duration.ofMinutes(0);
    private static final Duration RUN_INTERVAL = Duration.ofMinutes(5);

    CapacityProvider capacityProvider;

    List<MetalServerInternal> servers;

    public CapacityCache(CapacityProvider capacityProvider, ScheduledExecutorService scheduler) {
        this.capacityProvider = capacityProvider;
        scheduler.scheduleAtFixedRate(this::update, INITIAL_DELAY.toMinutes(),
                RUN_INTERVAL.toMinutes() , TimeUnit.MINUTES);
    }

    public CapacityCache(CapacityProvider capacityProvider) {
        this(capacityProvider, Executors.newSingleThreadScheduledExecutor());
    }

    public List<MetalServerInternal> getMetalServers() {
        return this.servers;
    }

    public void update() {
       try {
           log.info("Refreshing the capacity cache");
           servers = capacityProvider.loadServerData();
           log.info("Loaded {} MetalServer entries into the cache", servers.size());
       } catch (Exception e) {
           log.error("Failed to refresh capacity cache", e);
       }
    }
}
