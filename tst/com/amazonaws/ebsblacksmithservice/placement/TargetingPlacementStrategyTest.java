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

import com.amazonaws.ebsblacksmithservice.ServerDescriptor;
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
        final List<MetalServerInternal> servers =
            invokeWithPrimaryServerIp(Collections.singletonList(UUID.randomUUID().toString()));
        assertPlacementServerResponse(servers, 0);
    }

    @Test
    void testPlacementWithEmptyTargetServerIpInTargetingOptions() {
        final List<MetalServerInternal> servers =
            invokeWithPrimaryServerIp(Collections.emptyList());
        assertPlacementServerResponse(servers, SERVER_COUNT);
    }

    @Test
    void testPlacementWithNoServerDescriptorInTargetingOptions() {
        final PlacementOptions options = PlacementOptions.builder()
            .targetingOptions(new TargetingOptionsInternal(
                TargetingOptions.builder()
                    .build()))
            .build();
        final List<MetalServerInternal> servers = placement.placementServers(options);
        assertPlacementServerResponse(servers, SERVER_COUNT);
    }

    @Test
    void testPlacementWithValidServerIpInTargetingOptions() {

        final List<MetalServerInternal> metalServers = cache.getMetalServers();
        final List<MetalServerInternal> servers =
            invokeWithPrimaryServerIp(Collections.singletonList(metalServers.get(0).getIp()));
        assertPlacementServerResponse(servers, 1);
    }

    @Test
    void testPlacementWithInvalidServerAddressAndTargetingOptions() {

        when(cache.getMetalServers()).thenReturn(
            Collections.singletonList(
                MetalServerInternal.builder()
                    .serverAddress(UUID.randomUUID().toString())
                    .build()));
        final RuntimeException exception =
            assertThrows(RuntimeException.class,
                () -> invokeWithPrimaryServerIp(Collections.singletonList(UUID.randomUUID().toString())));
        assertTrue(exception.getMessage().contains("Invalid server address"));
    }

    @Test
    void testPlacementWithValidDiskServerIpInTargetingOptions() {
        final List<MetalDiskInternal> metalDisks = cache.getMetalDisks();
        assertPlacementDiskResponse(metalDisks.get(0).getDiskServerIp(), 1);
    }

    @Test
    void testPlacementWithInvalidDiskServerIpInTargetingOptions() {
        assertPlacementDiskResponse(UUID.randomUUID().toString(), 0);
    }

    private List<MetalServerInternal> invokeWithPrimaryServerIp(final List<String> primaryServerIpList) {
        final ServerDescriptor targetPrimaryServer = ServerDescriptor.builder()
            .withServerId(UUID.randomUUID().toString())
            .withIpAddresses(primaryServerIpList)
            .build();
        final PlacementOptions options = PlacementOptions.builder()
            .targetingOptions(new TargetingOptionsInternal(
                TargetingOptions.builder()
                    .withTargetPrimaryServer(targetPrimaryServer)
                    .build()))
            .build();
        return placement.placementServers(options);
    }

    private void assertPlacementServerResponse(final List<MetalServerInternal> servers, final int count) {
        assertNotNull(servers);
        assertEquals(count, servers.size());
    }

    private void assertPlacementDiskResponse(final String targetDiskId, final int count) {
        final ServerDescriptor targetPrimaryServerWithValidDiskServerIp =
            ServerDescriptor.builder()
                .withServerId(UUID.randomUUID().toString())
                .withIpAddresses(Collections.singletonList(targetDiskId))
                .build();
        final PlacementOptions options = PlacementOptions.builder()
            .targetingOptions(new TargetingOptionsInternal(
                TargetingOptions.builder()
                    .withTargetPrimaryServer(targetPrimaryServerWithValidDiskServerIp)
                    .build()))
            .diskResponseSizeRequested(REQUESTED_DISK_COUNT)
            .build();
        final List<MetalDiskInternal> disks = placement.placementDisks(options);
        assertNotNull(disks);
        assertEquals(count, disks.size());
    }
}
