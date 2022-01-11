package com.amazonaws.ebsblacksmithservice.activity;

import com.amazon.coral.annotation.Operation;
import com.amazon.coral.annotation.Service;
import com.amazon.coral.service.Activity;
import com.amazon.coral.service.LogRequests;
import com.amazon.coral.validate.Validated;
import com.amazonaws.ebsblacksmithservice.GetPlacementForMetalVolumeRequest;
import com.amazonaws.ebsblacksmithservice.GetPlacementForMetalVolumeResponse;

@Service("EbsBlacksmithService")
public class GetPlacementForMetalVolumeActivity extends Activity {

    @Operation("GetPlacementForMetalVolume")
    @Validated
    @LogRequests
    public GetPlacementForMetalVolumeResponse enact(GetPlacementForMetalVolumeRequest request) {
        return GetPlacementForMetalVolumeResponse.builder()
                .withHardwareId("12345")
                .build();
    }
}
