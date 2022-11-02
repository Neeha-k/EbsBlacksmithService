package com.amazonaws.ebsblacksmithservice.types;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import com.amazonaws.ebsblacksmithservice.MetalServer;

@Builder
@Getter
@Jacksonized
public class MetalServerInternal {
    private static final String COLON = ":";
    private final String serverAddress;

    public static List<MetalServer> toCoralModel(
        final List<MetalServerInternal> metalServerInternals) {
        return metalServerInternals
            .stream()
            .map(MetalServerInternal::toCoralModel)
            .collect(Collectors.toList());
    }

    public static MetalServer toCoralModel(
        final MetalServerInternal metalServerInternal) {
        return MetalServer.builder()
            .withIpAddress(metalServerInternal.getServerAddress())
            .build();
    }

    public String getIp() {
        final int separationIndexOfIpAndPort = this.serverAddress.indexOf(COLON);
        if (separationIndexOfIpAndPort < 0) {
            throw new RuntimeException(String.format("Invalid server address: %s", this.serverAddress));
        }
        return this.serverAddress.substring(0, separationIndexOfIpAndPort);
    }
}
