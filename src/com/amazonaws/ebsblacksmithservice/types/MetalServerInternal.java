package com.amazonaws.ebsblacksmithservice.types;

import com.amazonaws.ebsblacksmithservice.MetalServer;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
@Jacksonized
public class MetalServerInternal {
    private String ipAddress;

    private int availableDisks;

    public static List<MetalServer> toCoralModel(List<MetalServerInternal> metalServerInternals) {
        return metalServerInternals.stream().map(MetalServerInternal::toCoralModel).collect(Collectors.toList());
    }

    public static MetalServer toCoralModel(MetalServerInternal metalServerInternal) {
        return MetalServer.builder()
                .withIpAddress(metalServerInternal.getIpAddress())
                .build();
    }
}
