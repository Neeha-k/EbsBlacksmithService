package com.amazonaws.ebsblacksmithservice.activity;

import com.amazon.coral.annotation.Operation;
import com.amazon.coral.annotation.Service;
import com.amazon.coral.metrics.Metrics;
import com.amazon.coral.service.Activity;
import com.amazon.coral.service.LogRequests;
import com.amazon.coral.validate.Validated;
import com.amazonaws.ebsblacksmithservice.GetPlacementForMetalVolumeRequest;
import com.amazonaws.ebsblacksmithservice.GetPlacementForMetalVolumeResponse;
import com.amazonaws.ebsblacksmithservice.MetalDisk;
import com.amazonaws.ebsblacksmithservice.placement.PlacementOptions;
import com.amazonaws.ebsblacksmithservice.placement.PlacementStrategy;
import com.amazonaws.ebsblacksmithservice.types.MetalServerInternal;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import javax.measure.unit.Unit;
import java.util.List;
import java.util.stream.Collectors;

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
        final PlacementOptions options = PlacementOptions.builder()
                .responseSizeRequested(RESPONSE_SIZE)
                .customerId(request.getCustomerId())
                .build();

        final List<MetalServerInternal> servers = placementStrategy.place(options);

        final List<MetalDisk> availableDisks = servers.stream()
                .flatMap(metalServerInternal -> metalServerInternal
                        .toMetalDiskCoralModel()
                        .stream())
                .collect(Collectors.toList());


        final Metrics metrics = getMetrics();
        if (availableDisks.size() < RESPONSE_SIZE) {
            metrics.addCount(RECOMMENDATIONS_MISSING, RESPONSE_SIZE - servers.size(), Unit.ONE);
        }
        metrics.addCount(RECOMMENDATIONS_ACTUAL, availableDisks.size(), Unit.ONE);
        metrics.addCount(RECOMMENDATIONS_MAX, RESPONSE_SIZE, Unit.ONE);

        return GetPlacementForMetalVolumeResponse.builder()
                .withMetalServerRecommendations(MetalServerInternal.toCoralModel(servers))
                .withMetalDiskRecommendations(availableDisks)
                .build();
    }
}
