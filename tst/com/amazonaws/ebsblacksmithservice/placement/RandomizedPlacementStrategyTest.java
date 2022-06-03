package com.amazonaws.ebsblacksmithservice.placement;

import com.amazonaws.ebsblacksmithservice.capacity.CapacityCache;
import com.amazonaws.ebsblacksmithservice.placement.PlacementOptions;
import com.amazonaws.ebsblacksmithservice.placement.RandomizedPlacementStrategy;
import com.amazonaws.ebsblacksmithservice.types.MetalServerInternal;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class RandomizedPlacementStrategyTest {
    @Mock
    CapacityCache cache;

    RandomizedPlacementStrategy placement;

    private static final int SERVER_COUNT = 10;
    private static final int REQUESTED_SERVER_COUNT = 5;
    private static final int MAX_SERVER_CAPACITY = 22;
    private static final ThreadLocalRandom random = ThreadLocalRandom.current();

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        placement = new RandomizedPlacementStrategy(cache);
    }

    @Test
    void testSubsequentCallsReturnsDifferentOrdering() {
        int largeServerCount = 1000;
        Mockito.when(cache.getMetalServers()).thenReturn(generateMetalServers(largeServerCount));

        PlacementOptions options = PlacementOptions.builder().responseSizeRequested(largeServerCount).build();
        List<MetalServerInternal> firstResults = placement.place(options);
        List<MetalServerInternal> secondResults = placement.place(options);
        Assertions.assertEquals(firstResults.size(), secondResults.size());
        Assertions.assertNotEquals(firstResults, secondResults);
    }

    @Test
    void testPlacementReturnsCorrectCount() {
        Mockito.when(cache.getMetalServers()).thenReturn(generateMetalServers(SERVER_COUNT));

        PlacementOptions options = PlacementOptions.builder().responseSizeRequested(REQUESTED_SERVER_COUNT).build();
        List<MetalServerInternal> results = placement.place(options);
        Assertions.assertEquals(REQUESTED_SERVER_COUNT, results.size());
    }

    @Test
    void testPlacementWithFewerResultsThanRequested() {
        List<MetalServerInternal> metalServers = generateMetalServers(SERVER_COUNT);
        Mockito.when(cache.getMetalServers()).thenReturn(metalServers);

        PlacementOptions options = PlacementOptions.builder().responseSizeRequested(SERVER_COUNT + 1).build();
        List<MetalServerInternal> results = placement.place(options);
        Assertions.assertEquals(SERVER_COUNT, results.size());
    }

    @Test
    void testPlacementFiltersOutFullServers() {
        List<MetalServerInternal> metalServers = ImmutableList.of(MetalServerInternal.builder().availableDisks(0).build());
        Mockito.when(cache.getMetalServers()).thenReturn(metalServers);

        PlacementOptions options = PlacementOptions.builder().responseSizeRequested(SERVER_COUNT).build();
        List<MetalServerInternal> results = placement.place(options);
        Assertions.assertEquals(0, results.size());
    }

    private List<MetalServerInternal> generateMetalServers(int count) {
        List<MetalServerInternal> servers = new ArrayList<>();
        for (int i=0; i<count; i++) {
            servers.add(generateMetalServerWithRandomData());
        }
        return servers;
    }

    private MetalServerInternal generateMetalServerWithRandomData() {
        return MetalServerInternal
                .builder()
                .availableDisks(random.nextInt(1, MAX_SERVER_CAPACITY))
                .ipAddress(UUID.randomUUID().toString())
                .build();
    }
}
