package com.amazonaws.ebsblacksmithservice.capacity;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import com.amazonaws.ebsblacksmithservice.types.MetalDiskInternal;
import com.amazonaws.ebsblacksmithservice.types.MetalServerInternal;

/**
 * CapacityCache holds an in-memory view of the Metal Disk to be used for placement along with all Metal servers. Metal
 * servers are used for deletion of volume in metal server, as of today we perform delete volume calls across all
 * servers to handle any duplicate volume created across server.s This cache uses the CapacityProvider to replace its
 * data with fresh data.
 *
 * For now, this cache maintains a simple List of metal disk for placement, but future improvements can be added to bake
 * in data center topology to support more optimized queries instead of iterating through the list. The Metal server
 * deletion logic will also be replaced by location mapping of volume, and then we will not need to vend out servers for
 * deletion.
 */
@Slf4j
public class CapacityCache {

    private static final Duration INITIAL_DELAY = Duration.ofMinutes(5);
    private static final Duration RUN_INTERVAL = Duration.ofMinutes(5);

    private final CapacityProvider capacityProvider;

    List<MetalServerInternal> metalServers;
    List<MetalDiskInternal> metalDisks;

    public CapacityCache(CapacityProvider capacityProvider, ScheduledExecutorService scheduler) {
        this.capacityProvider = capacityProvider;
        this.update();
        scheduler.scheduleAtFixedRate(this::update, INITIAL_DELAY.toMinutes(),
            RUN_INTERVAL.toMinutes(), TimeUnit.MINUTES);
    }

    public CapacityCache(CapacityProvider capacityProvider) {
        this(capacityProvider, Executors.newSingleThreadScheduledExecutor());
    }

    public void update() {
        try {
            log.info("Refreshing the capacity cache");
            metalServers = capacityProvider.loadServerData();
            log.info("Loaded {} MetalServer into the cache", metalServers.size());
            metalDisks = capacityProvider.loadDiskData();
            log.info("Loaded {} MetalDisks into the cache", metalDisks.size());
        } catch (final Exception e) {
            log.error("Failed to refresh capacity cache", e);
        }
    }

    public List<MetalServerInternal> getMetalServers() {
        return new ArrayList<>(metalServers);
    }

    public List<MetalDiskInternal> getMetalDisks() {
        return new ArrayList<>(metalDisks);
    }
}
