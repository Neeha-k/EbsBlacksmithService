package com.amazonaws.ebsblacksmithservice.capacity;

import static com.amazonaws.ebsblacksmithservice.util.TestDataGenerator.generateMetalDisks;
import static com.amazonaws.ebsblacksmithservice.util.TestDataGenerator.generateMetalServers;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import com.amazon.coral.metrics.Metrics;
import com.amazon.coral.metrics.MetricsFactory;

import com.amazonaws.ebsblacksmithservice.types.MetalServerInternal;
import com.amazonaws.ebsblacksmithservice.types.MetalDiskInternal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.measure.unit.Unit;

public class CapacityCacheTest {

    private static final String FAILURE_METRIC_NAME = "Failure";

    @Mock
    CapacityProvider capacityProvider;

    @Mock
    ScheduledExecutorService schedulerMock;

    @Mock
    private MetricsFactory metricsFactory;
    @Mock
    private Metrics metrics;

    CapacityCache cache;

    @BeforeEach
    void setup() {
        openMocks(this);
        reset(capacityProvider, schedulerMock, metrics);
        when(capacityProvider.loadServerData()).thenReturn(generateMetalServers(1));
        when(capacityProvider.loadDiskData()).thenReturn(generateMetalDisks(1));
        when(metricsFactory.newMetrics()).thenReturn(metrics);
        cache = new CapacityCache(capacityProvider, schedulerMock, metricsFactory);
    }

    @Test
    void schedulerInvocation() {
        verify(schedulerMock).scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(),
            any(TimeUnit.class));
    }

    @Test
    void cacheHydratedAtCreation() {

        final List<MetalServerInternal> metalServers = cache.getMetalServers();
        final List<MetalDiskInternal> metalDisk = cache.getMetalDisks();

        assertEquals(1, metalServers.size());
        verify(capacityProvider).loadServerData();
        verify(metrics, atLeastOnce()).addCount(FAILURE_METRIC_NAME, 0.0, Unit.ONE);

        assertEquals(1, metalDisk.size());
        verify(capacityProvider).loadDiskData();
        verify(metrics, atLeastOnce()).addCount(FAILURE_METRIC_NAME, 0.0, Unit.ONE);
    }

    @Test
    void updateReplacesCache() {
        final List<MetalServerInternal> metalServersInitial = cache.getMetalServers();
        when(capacityProvider.loadServerData()).thenReturn(generateMetalServers(1));

        cache.update();

        final List<MetalServerInternal> metalServersOnSubsequentCall = cache.getMetalServers();
        assertNotEquals(metalServersInitial, metalServersOnSubsequentCall);
        verify(capacityProvider, times(2)).loadServerData();
        verify(metrics, atLeastOnce()).addCount(FAILURE_METRIC_NAME, 0.0, Unit.ONE);
    }

    @Test
    void cacheLoadThrowsException() {
        when(capacityProvider.loadServerData()).thenThrow(RuntimeException.class);

        cache.update();

        verify(capacityProvider, times(2)).loadServerData();
        verify(metrics, atLeastOnce()).addCount(FAILURE_METRIC_NAME, 1.0, Unit.ONE);
    }
}
