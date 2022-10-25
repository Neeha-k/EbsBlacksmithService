package com.amazonaws.ebsblacksmithservice.activity;

import static com.amazonaws.ebsblacksmithservice.placement.PlacementStrategyDecider.registerPlacementStrategy;
import static com.amazonaws.ebsblacksmithservice.placement.PlacementStrategyType.RANDOM;
import static com.amazonaws.ebsblacksmithservice.placement.PlacementStrategyType.TARGETING;
import static com.amazonaws.ebsblacksmithservice.util.TestDataGenerator.generateMetalDisks;
import static com.amazonaws.ebsblacksmithservice.util.TestDataGenerator.generateMetalServers;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.UUID;

import javax.measure.unit.Unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.amazon.coral.metrics.Metrics;
import com.amazon.coral.model.basic.BasicOperationModel;
import com.amazon.coral.model.basic.BasicServiceModel;
import com.amazon.coral.service.Context;

import com.amazonaws.ebsblacksmithservice.GetPlacementForMetalVolumeRequest;
import com.amazonaws.ebsblacksmithservice.InsufficientCapacityException;
import com.amazonaws.ebsblacksmithservice.InvalidArgumentException;
import com.amazonaws.ebsblacksmithservice.ServerDescriptor;
import com.amazonaws.ebsblacksmithservice.TargetingOptions;
import com.amazonaws.ebsblacksmithservice.placement.PlacementOptions;
import com.amazonaws.ebsblacksmithservice.placement.RandomizedPlacementStrategy;
import com.amazonaws.ebsblacksmithservice.placement.TargetingPlacementStrategy;

public class GetPlacementForMetalVolumeActivityUnitTest {

    @Mock
    private RandomizedPlacementStrategy randomizedPlacementStrategy;

    @Mock
    private TargetingPlacementStrategy targetingPlacementStrategy;

    @Mock
    private Metrics metrics;

    private GetPlacementForMetalVolumeActivity activity;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        Context ctx = new Context(new BasicServiceModel(), new BasicOperationModel(), null);
        ctx.setMetrics(metrics);
        registerPlacementStrategy(RANDOM, randomizedPlacementStrategy);
        registerPlacementStrategy(TARGETING, targetingPlacementStrategy);
        this.activity = new GetPlacementForMetalVolumeActivity();
        this.activity.setContext(ctx);
        reset(randomizedPlacementStrategy, targetingPlacementStrategy, metrics);
    }

    @Test
    public void testMetricsPublishWhenRecommendedDisksAreOfResponseSize() {
        when(randomizedPlacementStrategy.placementDisks(isA(PlacementOptions.class))).thenReturn(generateMetalDisks(1600));
        this.activity.enact(GetPlacementForMetalVolumeRequest
            .builder()
            .withVolumeId(UUID.randomUUID().toString())
            .withCustomerId(UUID.randomUUID().toString())
            .build());
        verify(metrics).addCount("RecommendationsMax", 1600, Unit.ONE);
        verify(metrics).addCount("RecommendationsActual", 1600, Unit.ONE);
    }

    @Test
    public void testGetPlacementForInsufficientCapacity() {
        when(targetingPlacementStrategy.placementServers(isA(PlacementOptions.class))).thenReturn(generateMetalServers(1));
        when(targetingPlacementStrategy.placementDisks(isA(PlacementOptions.class))).thenReturn(Collections.emptyList());
        final ServerDescriptor targetPrimaryServer =
            ServerDescriptor.builder()
                .withServerId(UUID.randomUUID().toString())
                .withIpAddresses(Collections.singletonList(UUID.randomUUID().toString()))
                .build();
        assertThrows(InsufficientCapacityException.class, () -> this.activity.enact(GetPlacementForMetalVolumeRequest
            .builder()
            .withVolumeId(UUID.randomUUID().toString())
            .withCustomerId(UUID.randomUUID().toString())
            .withTargetingOptions(
                TargetingOptions.builder()
                    .withTargetPrimaryServer(targetPrimaryServer)
                    .build())
            .build()));
        verify(metrics).addCount("InsufficientCapacity", true);
    }

    @Test
    public void testGetPlacementForInvalidTargetServer() {
        when(targetingPlacementStrategy.placementServers(isA(PlacementOptions.class))).thenReturn(Collections.emptyList());
        when(targetingPlacementStrategy.placementDisks(isA(PlacementOptions.class))).thenReturn(Collections.emptyList());
        final ServerDescriptor targetPrimaryServer =
            ServerDescriptor.builder()
                .withServerId(UUID.randomUUID().toString())
                .withIpAddresses(Collections.singletonList(UUID.randomUUID().toString()))
                .build();
        assertThrows(InvalidArgumentException.class, () -> this.activity.enact(GetPlacementForMetalVolumeRequest
            .builder()
            .withVolumeId(UUID.randomUUID().toString())
            .withCustomerId(UUID.randomUUID().toString())
            .withTargetingOptions(
                TargetingOptions.builder()
                    .withTargetPrimaryServer(targetPrimaryServer)
                    .build())
            .build()));
        verify(metrics, never()).addCount("InsufficientCapacity", true);
    }
}
