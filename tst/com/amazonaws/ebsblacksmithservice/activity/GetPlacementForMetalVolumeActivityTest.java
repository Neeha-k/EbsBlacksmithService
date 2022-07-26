package com.amazonaws.ebsblacksmithservice.activity;

import com.amazonaws.ebsblacksmithservice.AbstractFunctionalTestCase;
import com.amazonaws.ebsblacksmithservice.GetPlacementForMetalVolumeRequest;
import com.amazonaws.ebsblacksmithservice.GetPlacementForMetalVolumeResponse;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class GetPlacementForMetalVolumeActivityTest extends AbstractFunctionalTestCase {

    @Test
    public void testEnact() {
        final GetPlacementForMetalVolumeResponse response =
                this.activity.enact(GetPlacementForMetalVolumeRequest
                        .builder()
                        .withVolumeId(UUID.randomUUID().toString())
                        .withCustomerId(UUID.randomUUID().toString())
                        .build());
        assertPlacementResponse(response);
    }

    @Test
    public void testEnactForShuffledResponse() {
        final GetPlacementForMetalVolumeResponse response =
                this.activity.enact(GetPlacementForMetalVolumeRequest
                        .builder()
                        .withVolumeId(UUID.randomUUID().toString())
                        .withCustomerId(UUID.randomUUID().toString())
                        .build());
        assertPlacementResponse(response);

        final GetPlacementForMetalVolumeResponse response1 =
                this.activity.enact(GetPlacementForMetalVolumeRequest
                        .builder()
                        .withVolumeId(UUID.randomUUID().toString())
                        .withCustomerId(UUID.randomUUID().toString())
                        .build());
        assertPlacementResponse(response1);
        assertEquals(response.getMetalServerRecommendations().size(),
                response1.getMetalServerRecommendations().size());
        assertEquals(response.getMetalDiskRecommendations().size(),
                response1.getMetalDiskRecommendations().size());

        assumeTrue(response.getMetalServerRecommendations().size() > 1 ||
                response.getMetalDiskRecommendations().size() > 1);
        assertNotEquals(response, response1);

        assertNotEquals(response.getMetalDiskRecommendations(), response1.getMetalDiskRecommendations());
    }

    private void assertPlacementResponse(final GetPlacementForMetalVolumeResponse response) {
        assertNotNull(response);
        assertNotNull(response.getMetalServerRecommendations());
        assertNotNull(response.getMetalDiskRecommendations());
    }
}
