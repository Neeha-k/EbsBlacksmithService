package com.amazonaws.ebsblacksmithservice.types;

import com.amazon.aws.authruntimeclient.internal.collections4.ListUtils;
import com.amazonaws.ebsblacksmithservice.MetalDisk;
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
    private final String ipAddress;

    private final int availableDisks;

    private final List<MetalDiskInternal> metalDisks;

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
                .withIpAddress(metalServerInternal.getIpAddress())
                .build();
    }

    public List<MetalDisk> toMetalDiskCoralModel() {
        return this.metalDisks.stream()
                .map(metalDiskInternal -> MetalDisk
                        .builder()
                        .withLogicalDiskId(metalDiskInternal.getLogicalDiskId())
                        .withDiskServerAddress(this.ipAddress)
                        .build())
                .collect(Collectors.toList());
    }

    public static List<MetalServerInternal> deepCopy(
            final List<MetalServerInternal> servers) {

        return ListUtils.emptyIfNull(servers)
                .stream()
                .map(server -> MetalServerInternal.builder()
                        .ipAddress(server.getIpAddress())
                        .availableDisks(server.getAvailableDisks())
                        .metalDisks(MetalDiskInternal.deepCopy(
                                server.getMetalDisks()))
                        .build())
                .collect(Collectors.toList());
    }
}
