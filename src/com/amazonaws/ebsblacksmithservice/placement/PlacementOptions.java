package com.amazonaws.ebsblacksmithservice.placement;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PlacementOptions {
    private int responseSizeRequested;
    private String customerId;
}
