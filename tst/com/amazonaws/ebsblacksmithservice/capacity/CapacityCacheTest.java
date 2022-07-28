package com.amazonaws.ebsblacksmithservice.capacity;

import static com.amazonaws.ebsblacksmithservice.util.TestDataGenerator.generateMetalDisks;
import static com.amazonaws.ebsblacksmithservice.util.TestDataGenerator.generateMetalServers;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.amazonaws.ebsblacksmithservice.types.MetalDiskInternal;
import com.amazonaws.ebsblacksmithservice.types.MetalServerInternal;

public class CapacityCacheTest {
    @Mock
    CapacityProvider capacityProvider;

    @Mock
    ScheduledExecutorService schedulerMock;

    CapacityCache cache;

    @BeforeEach
    void setup() {
        openMocks(this);
        reset(capacityProvider, schedulerMock);
        when(capacityProvider.loadServerData()).thenReturn(generateMetalServers(1));
        when(capacityProvider.loadDiskData()).thenReturn(generateMetalDisks(1));
        cache = new CapacityCache(capacityProvider, schedulerMock);
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

        assertEquals(1, metalDisk.size());
        verify(capacityProvider).loadDiskData();
    }

    @Test
    void updateReplacesCache() {
        final List<MetalServerInternal> metalServersInitial = cache.getMetalServers();
        when(capacityProvider.loadServerData()).thenReturn(generateMetalServers(1));

        cache.update();

        final List<MetalServerInternal> metalServersOnSubsequentCall = cache.getMetalServers();
        assertNotEquals(metalServersInitial, metalServersOnSubsequentCall);
        verify(capacityProvider, times(2)).loadServerData();
    }
}
