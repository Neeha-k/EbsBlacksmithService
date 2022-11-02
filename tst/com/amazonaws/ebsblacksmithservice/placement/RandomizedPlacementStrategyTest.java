package com.amazonaws.ebsblacksmithservice.placement;

import static com.amazonaws.ebsblacksmithservice.util.TestDataGenerator.generateMetalDisks;
import static com.amazonaws.ebsblacksmithservice.util.TestDataGenerator.generateMetalServers;
import static com.amazonaws.ebsblacksmithservice.util.TestDataGenerator.generateMetalDiskWithServerAddress;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.amazonaws.ebsblacksmithservice.capacity.CapacityCache;
import com.amazonaws.ebsblacksmithservice.types.MetalDiskInternal;
import com.amazonaws.ebsblacksmithservice.types.MetalServerInternal;

public class RandomizedPlacementStrategyTest {
    @Mock
    CapacityCache cache;

    RandomizedPlacementStrategy placement;

    private static final int SERVER_COUNT = 10;
    private static final int REQUESTED_DISK_COUNT = 5;
    private static final int DISK_COUNT = 22;

    @BeforeEach
    void setup() {
        openMocks(this);
        reset(cache);
        placement = new RandomizedPlacementStrategy(cache);
        when(cache.getMetalServers()).thenReturn(generateMetalServers(SERVER_COUNT));
        when(cache.getMetalDisks()).thenReturn(generateMetalDisks(DISK_COUNT));
    }

    @Test
    void testSubsequentCallsReturnsDifferentOrdering() {
        final PlacementOptions options = PlacementOptions.builder()
            .build();
        final List<MetalServerInternal> servers = placement.placementServers(options);
        final List<MetalServerInternal> serversOnSubsequentCall = placement.placementServers(options);

        final List<MetalDiskInternal> disks = getPlacementDiskWithRequestedCount(REQUESTED_DISK_COUNT);
        final List<MetalDiskInternal> disksOnSubsequentCall = getPlacementDiskWithRequestedCount(REQUESTED_DISK_COUNT);

        assertEquals(servers.size(), SERVER_COUNT);
        assertEquals(servers.size(), serversOnSubsequentCall.size());
        assertEquals(servers, serversOnSubsequentCall);
        verify(cache, times(2)).getMetalServers();

        assertEquals(disks.size(), REQUESTED_DISK_COUNT);
        assertEquals(disks.size(), disksOnSubsequentCall.size());
        assertNotEquals(disks, disksOnSubsequentCall);
        verify(cache, times(2)).getMetalDisks();
    }

    @Test
    void testPlacementReturnsRequestedCount_WhenAvailableDisksIsMoreThanRequested() {
        final List<MetalDiskInternal> disks = getPlacementDiskWithRequestedCount(REQUESTED_DISK_COUNT);
        assertNotNull(disks);
        assertEquals(REQUESTED_DISK_COUNT, disks.size());
        verify(cache).getMetalDisks();
    }

    @Test
    void testPlacementReturnsAllDisks_WhenAvailableDisksIsLessThanRequested() {
        final List<MetalDiskInternal> disks =
            getPlacementDiskWithRequestedCount(DISK_COUNT + REQUESTED_DISK_COUNT);
        assertNotNull(disks);
        assertEquals(DISK_COUNT, disks.size());
        verify(cache).getMetalDisks();
    }

    @Test
    void testPlacementReturnsDisksExcludingTargetedOnlyServer() {
        List<MetalDiskInternal> generatedDisks = generateMetalDisks(REQUESTED_DISK_COUNT);
        Random rand = new Random();

        String serverAddress = PlacementStrategy.TARGETED_ONLY_PLACEMENT_SERVER_ADDRESSES.get(
            rand.nextInt(PlacementStrategy.TARGETED_ONLY_PLACEMENT_SERVER_ADDRESSES.size()));

        generatedDisks.set(0, generateMetalDiskWithServerAddress(serverAddress));
        when(cache.getMetalDisks()).thenReturn(generatedDisks);

        final List<MetalDiskInternal> disks = getPlacementDiskWithRequestedCount(REQUESTED_DISK_COUNT);
        assertNotNull(disks);
        assertEquals(REQUESTED_DISK_COUNT - 1, disks.size());
        assertTrue(disks.stream().anyMatch(disk -> !PlacementStrategy.TARGETED_ONLY_PLACEMENT_SERVER_ADDRESSES.contains(disk.getServerAddress())));
        verify(cache).getMetalDisks();
    }

    private List<MetalDiskInternal> getPlacementDiskWithRequestedCount(final int requestedCount) {
        final PlacementOptions options = PlacementOptions.builder()
            .diskResponseSizeRequested(requestedCount)
            .build();
        return placement.placementDisks(options);
    }
}
