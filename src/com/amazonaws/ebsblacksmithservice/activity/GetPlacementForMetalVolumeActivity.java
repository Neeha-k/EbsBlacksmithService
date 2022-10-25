package com.amazonaws.ebsblacksmithservice.activity;

import java.util.List;

import javax.inject.Inject;
import javax.measure.unit.Unit;

import lombok.RequiredArgsConstructor;

import com.amazon.coral.annotation.Operation;
import com.amazon.coral.annotation.Service;
import com.amazon.coral.metrics.Metrics;
import com.amazon.coral.service.Activity;
import com.amazon.coral.service.LogRequests;
import com.amazon.coral.validate.Validated;

import com.amazonaws.ebsblacksmithservice.GetPlacementForMetalVolumeRequest;
import com.amazonaws.ebsblacksmithservice.GetPlacementForMetalVolumeResponse;
import com.amazonaws.ebsblacksmithservice.InsufficientCapacityException;
import com.amazonaws.ebsblacksmithservice.InvalidArgumentException;
import com.amazonaws.ebsblacksmithservice.placement.PlacementOptions;
import com.amazonaws.ebsblacksmithservice.placement.PlacementStrategy;
import com.amazonaws.ebsblacksmithservice.placement.PlacementStrategyDecider;
import com.amazonaws.ebsblacksmithservice.placement.TargetingOptionsInternal;
import com.amazonaws.ebsblacksmithservice.types.MetalDiskInternal;
import com.amazonaws.ebsblacksmithservice.types.MetalServerInternal;

@Service("EbsBlacksmithService")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GetPlacementForMetalVolumeActivity extends Activity {

    // For now we can hardcode the recommendation response size to simplify our external facing API.
    // In the future, we will want to tune the number of responses returned by blacksmith based upon our
    // confidence of one of them being successful
    // We want to pick a number larger than the number of disks we will have.
    private static final int RESPONSE_SIZE = 1600;

    private static final String RECOMMENDATIONS_MISSING = "RecommendationsMissing";
    private static final String RECOMMENDATIONS_ACTUAL = "RecommendationsActual";
    private static final String RECOMMENDATIONS_MAX = "RecommendationsMax";
    private static final String INSUFFICIENT_CAPACITY = "InsufficientCapacity";

    @Operation("GetPlacementForMetalVolume")
    @Validated
    @LogRequests
    public GetPlacementForMetalVolumeResponse enact(GetPlacementForMetalVolumeRequest request) {
        final PlacementOptions placementOptions = PlacementOptions.builder()
            .diskResponseSizeRequested(RESPONSE_SIZE)
            .targetingOptions(
                new TargetingOptionsInternal(request.getTargetingOptions()))
            .build();

        final PlacementStrategy placementStrategy = PlacementStrategyDecider.getPlacementStrategy(placementOptions);

        final List<MetalServerInternal> servers = placementStrategy.placementServers(placementOptions);

        final List<MetalDiskInternal> disks = placementStrategy.placementDisks(placementOptions);

        validatePlacementResponse(servers, disks, placementOptions, request.getVolumeId());

        publishMetricsForRecommendedDiskSize(disks.size());

        return GetPlacementForMetalVolumeResponse.builder()
            .withMetalServerRecommendations(MetalServerInternal.toCoralModel(servers))
            .withMetalDiskRecommendations(MetalDiskInternal.toCoralModel(disks))
            .build();
    }

    private void publishMetricsForRecommendedDiskSize(final int recommendedDiskSize) {
        final Metrics metrics = getMetrics();
        if (recommendedDiskSize < RESPONSE_SIZE) {
            metrics.addCount(RECOMMENDATIONS_MISSING, RESPONSE_SIZE - recommendedDiskSize, Unit.ONE);
        }
        metrics.addCount(RECOMMENDATIONS_ACTUAL, recommendedDiskSize, Unit.ONE);
        metrics.addCount(RECOMMENDATIONS_MAX, RESPONSE_SIZE, Unit.ONE);
    }

    private void validatePlacementResponse(
        final List<MetalServerInternal> placementServers,
        final List<MetalDiskInternal> placementDisk,
        final PlacementOptions placementOptions,
        final String volumeId) {
        if (placementOptions.hasTargetingOptionForPlacement() && placementServers.isEmpty()) {
            throw new InvalidArgumentException(String.format(
                "No available server found for placement of volume: %s with option: %s", volumeId, placementOptions));
        }
        if (placementDisk.isEmpty()) {
            final Metrics metrics = getMetrics();
            metrics.addCount(INSUFFICIENT_CAPACITY, true);
            throw new InsufficientCapacityException(String.format(
                "No available disk found for placement of volume: %s with option: %s", volumeId, placementOptions));
        }
    }
}
