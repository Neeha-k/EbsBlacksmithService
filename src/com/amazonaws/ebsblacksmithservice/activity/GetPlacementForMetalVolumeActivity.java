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
import com.amazonaws.ebsblacksmithservice.placement.PlacementOptions;
import com.amazonaws.ebsblacksmithservice.placement.PlacementStrategy;
import com.amazonaws.ebsblacksmithservice.types.MetalDiskInternal;
import com.amazonaws.ebsblacksmithservice.types.MetalServerInternal;

@Service("EbsBlacksmithService")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GetPlacementForMetalVolumeActivity extends Activity {

    @Inject
    PlacementStrategy placementStrategy;

    // For now we can hardcode the recommendation response size to simplify our external facing API.
    // In the future, we will want to tune the number of responses returned by blacksmith based upon our
    // confidence of one of them being successful
    // We want to pick a number larger than the number of disks we will have.
    private static final int RESPONSE_SIZE = 1600;

    private static final String RECOMMENDATIONS_MISSING = "RecommendationsMissing";
    private static final String RECOMMENDATIONS_ACTUAL = "RecommendationsActual";
    private static final String RECOMMENDATIONS_MAX = "RecommendationsMax";

    @Operation("GetPlacementForMetalVolume")
    @Validated
    @LogRequests
    public GetPlacementForMetalVolumeResponse enact(GetPlacementForMetalVolumeRequest request) {

        final List<MetalServerInternal> servers = placementStrategy.getAllMetalServers();

        final List<MetalDiskInternal> disks = placementStrategy.placementDisks(
            PlacementOptions.builder()
                .diskResponseSizeRequested(RESPONSE_SIZE)
                .build());

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
}
