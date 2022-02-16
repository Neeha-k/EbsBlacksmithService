package com.amazonaws.ebsblacksmithservice.types;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Builder
@Getter
@Jacksonized
public class MetalServerInternal {
    private String ipAddress;

    private int availableDisks;
}
