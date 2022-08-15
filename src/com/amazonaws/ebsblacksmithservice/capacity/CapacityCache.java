package com.amazonaws.ebsblacksmithservice.capacity;

import com.amazon.coral.metrics.Metrics;
import com.amazon.coral.metrics.MetricsFactory;
import com.amazonaws.ebsblacksmithservice.types.MetalServerInternal;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.measure.unit.Unit;
import javax.measure.unit.SI;

import com.amazonaws.ebsblacksmithservice.types.MetalDiskInternal;

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

    private static final String FAILURE_METRIC_NAME = "Failure";
    private static final String CACHE_SERVER_SIZE_METRIC_NAME = "CapacityServerCacheSize";
    private static final String CACHE_DISK_SIZE_METRIC_NAME = "CapacityDiskCacheSize";
    private static final String OPERATION_NAME = "CapacityCacheUpdate";
    private static final Duration INITIAL_DELAY = Duration.ofMinutes(5);
    private static final Duration RUN_INTERVAL = Duration.ofMinutes(5);
    private final MetricsFactory metricsFactory;

    private final CapacityProvider capacityProvider;

    List<MetalServerInternal> metalServers;
    List<MetalDiskInternal> metalDisks;

    public CapacityCache(CapacityProvider capacityProvider, ScheduledExecutorService scheduler,
            @NonNull MetricsFactory metricsFactory) {
        this.capacityProvider = capacityProvider;
        this.metricsFactory = metricsFactory;
        this.update();
        scheduler.scheduleAtFixedRate(this::update, INITIAL_DELAY.toMinutes(),
            RUN_INTERVAL.toMinutes(), TimeUnit.MINUTES);
    }

    public CapacityCache(CapacityProvider capacityProvider, @NonNull MetricsFactory metricsFactory) {
        this(capacityProvider, Executors.newSingleThreadScheduledExecutor(), metricsFactory);
    }

    public void update() {
        Metrics metrics = metricsFactory.newMetrics();

        final long startTime = System.currentTimeMillis();
        boolean updateSuccessful = false;

        try {
            log.info("Refreshing the capacity cache");
            metalServers = capacityProvider.loadServerData();
            log.info("Loaded {} MetalServer into the cache", metalServers.size());
            metalDisks = capacityProvider.loadDiskData();
            log.info("Loaded {} MetalDisks into the cache", metalDisks.size());
            updateSuccessful = true;
        } catch (final Exception e) {
            log.error("Failed to refresh capacity cache", e);
        } finally {
            final long endTime = System.currentTimeMillis();
            metrics.addProperty("Operation", OPERATION_NAME);
            metrics.addDate("StartTime", startTime);
            metrics.addDate("EndTime", endTime);
            metrics.addTime("Time", endTime - startTime, SI.MILLI(SI.SECOND));
            metrics.addCount(FAILURE_METRIC_NAME, updateSuccessful ? 0.0 : 1.0, Unit.ONE);
            metrics.addCount(CACHE_SERVER_SIZE_METRIC_NAME, metalServers.size(), Unit.ONE);
            metrics.addCount(CACHE_DISK_SIZE_METRIC_NAME, metalDisks.size(), Unit.ONE);
            metrics.close();
        }
    }

    public List<MetalServerInternal> getMetalServers() {
        return new ArrayList<>(metalServers);
    }

    public List<MetalDiskInternal> getMetalDisks() {
        return new ArrayList<>(metalDisks);
    }
}
