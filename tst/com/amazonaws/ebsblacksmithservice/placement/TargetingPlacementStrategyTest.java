package com.amazonaws.ebsblacksmithservice.placement;

import static com.amazonaws.ebsblacksmithservice.util.TestDataGenerator.generateMetalDisks;
import static com.amazonaws.ebsblacksmithservice.util.TestDataGenerator.generateMetalServers;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.amazonaws.ebsblacksmithservice.TargetingOptions;
import com.amazonaws.ebsblacksmithservice.capacity.CapacityCache;
import com.amazonaws.ebsblacksmithservice.types.MetalDiskInternal;
import com.amazonaws.ebsblacksmithservice.types.MetalServerInternal;

public class TargetingPlacementStrategyTest {
    @Mock
    CapacityCache cache;

    TargetingPlacementStrategy placement;

    private static final int SERVER_COUNT = 10;
    private static final int REQUESTED_DISK_COUNT = 5;
    private static final int DISK_COUNT = 22;

    @BeforeEach
    void setup() {
        openMocks(this);
        reset(cache);
        placement = new TargetingPlacementStrategy(cache);
        when(cache.getMetalServers()).thenReturn(generateMetalServers(SERVER_COUNT));
        when(cache.getMetalDisks()).thenReturn(generateMetalDisks(DISK_COUNT));
    }

    @Test
    void testPlacementWithRandomTargetServerIpInTargetingOptions() {
        final PlacementOptions options = PlacementOptions.builder()
            .targetingOptions(new TargetingOptionsInternal(
                TargetingOptions.builder()
                    .withTargetServerIp(UUID.randomUUID().toString())
                    .build()))
            .build();
        final List<MetalServerInternal> servers = placement.placementServers(options);
        assertNotNull(servers);
        assertTrue(servers.isEmpty());
    }

    @Test
    void testPlacementWithValidServerIpInTargetingOptions() {
        final List<MetalServerInternal> metalServers = cache.getMetalServers();
        final PlacementOptions options = PlacementOptions.builder()
            .targetingOptions(new TargetingOptionsInternal(
                TargetingOptions.builder()
                    .withTargetServerIp(metalServers.get(0).getIp())
                    .build()))
            .build();
        final List<MetalServerInternal> servers = placement.placementServers(options);
        assertNotNull(servers);
        assertEquals(servers.size(), 1);
    }

    @Test
    void testPlacementWithValidDiskServerIpInTargetingOptions() {
        final List<MetalDiskInternal> metalDisks = cache.getMetalDisks();
        final PlacementOptions options = PlacementOptions.builder()
            .targetingOptions(new TargetingOptionsInternal(
                TargetingOptions.builder()
                    .withTargetServerIp(metalDisks.get(0).getDiskServerIp())
                    .build()))
            .diskResponseSizeRequested(REQUESTED_DISK_COUNT)
            .build();
        final List<MetalDiskInternal> disks = placement.placementDisks(options);
        assertNotNull(disks);
        assertEquals(disks.size(), 1);
    }

    @Test
    void testPlacementWithInvalidServerAddressAndTargetingOptions() {
        when(cache.getMetalServers()).thenReturn(
            Collections.singletonList(
                MetalServerInternal.builder()
                    .serverAddress(UUID.randomUUID().toString())
                    .build()));
        final PlacementOptions options = PlacementOptions.builder()
            .targetingOptions(new TargetingOptionsInternal(
                TargetingOptions.builder()
                    .withTargetServerIp(UUID.randomUUID().toString())
                    .build()))
            .build();
        final RuntimeException exception = assertThrows(RuntimeException.class, () -> placement.placementServers(options));
        assertTrue(exception.getMessage().contains("Invalid server address"));
    }
}
