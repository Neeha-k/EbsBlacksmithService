package com.amazonaws.ebsblacksmithservice.activity;

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
import com.amazonaws.ebsblacksmithservice.types.MetalServerInternal;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import javax.measure.unit.Unit;
import java.util.List;

@Service("EbsBlacksmithService")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class GetPlacementForMetalVolumeActivity extends Activity {

    @Inject
    PlacementStrategy placementStrategy;

    // For now we can hardcode the recommendation response size to simplify our external facing API.
    // In the future, we will want to tune the number of responses returned by blacksmith based upon our
    // confidence of one of them being successful
    private static final int RESPONSE_SIZE = 1;

    private static final String RECOMMENDATIONS_MISSING = "RecommendationsMissing";

    @Operation("GetPlacementForMetalVolume")
    @Validated
    @LogRequests
    public GetPlacementForMetalVolumeResponse enact(GetPlacementForMetalVolumeRequest request) {
        PlacementOptions options = PlacementOptions.builder()
                .responseSizeRequested(RESPONSE_SIZE)
                .customerId(request.getCustomerId())
                .build();

        List<MetalServerInternal> servers = placementStrategy.place(options);

        Metrics metrics = getMetrics();
        if (servers.size() < RESPONSE_SIZE) {
            metrics.addCount(RECOMMENDATIONS_MISSING, RESPONSE_SIZE - servers.size(), Unit.ONE);
        }

        return GetPlacementForMetalVolumeResponse.builder()
                .withMetalServerRecommendations(MetalServerInternal.toCoralModel(servers))
                .build();
    }
}
