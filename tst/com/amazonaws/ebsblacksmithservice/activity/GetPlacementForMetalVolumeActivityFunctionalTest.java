package com.amazonaws.ebsblacksmithservice.activity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.amazonaws.ebsblacksmithservice.AbstractFunctionalTestCase;
import com.amazonaws.ebsblacksmithservice.GetPlacementForMetalVolumeRequest;
import com.amazonaws.ebsblacksmithservice.GetPlacementForMetalVolumeResponse;
import com.amazonaws.ebsblacksmithservice.ServerDescriptor;
import com.amazonaws.ebsblacksmithservice.TargetingOptions;

public class GetPlacementForMetalVolumeActivityFunctionalTest extends AbstractFunctionalTestCase {

    public static final String GAMMA_TARGET_SERVER_IP = "30.133.7.151";

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
    public void testEnactWithTargetingOptions() {
        final ServerDescriptor targetPrimaryServerWithGammaIp =
            ServerDescriptor.builder()
                .withServerId(UUID.randomUUID().toString())
                .withIpAddresses(Collections.singletonList(GAMMA_TARGET_SERVER_IP))
                .build();
        final GetPlacementForMetalVolumeResponse response =
            this.activity.enact(GetPlacementForMetalVolumeRequest
                .builder()
                .withVolumeId(UUID.randomUUID().toString())
                .withCustomerId(UUID.randomUUID().toString())
                .withTargetingOptions(
                    TargetingOptions.builder()
                        .withTargetPrimaryServer(targetPrimaryServerWithGammaIp)
                        .build())
                .build());
        assertNotNull(response);
        assertTrue(response.getMetalServerRecommendations()
            .stream()
            .allMatch(metalServer -> metalServer.getIpAddress()
                .contains(GAMMA_TARGET_SERVER_IP)));
        assertTrue(response.getMetalDiskRecommendations()
            .stream()
            .allMatch(metalDisk -> metalDisk.getDiskServerAddress()
                .contains(GAMMA_TARGET_SERVER_IP)));
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
