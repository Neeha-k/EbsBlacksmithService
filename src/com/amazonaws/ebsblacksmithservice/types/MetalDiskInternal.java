package com.amazonaws.ebsblacksmithservice.types;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import com.amazonaws.ebsblacksmithservice.MetalDisk;

/**
 * This class represents the Disk entity in the context of BlackSmith service. Attributes of disks in this class contain
 * (in-future will contain) all the information retrieved from Data-plane and needed for identifying the placement
 * strategy.
 */
@Getter
@Builder
@Jacksonized
public class MetalDiskInternal {
    private final String logicalDiskId;
    private final String serverAddress;

    public static List<MetalDisk> toCoralModel(
        final List<MetalDiskInternal> metalDiskInternals) {
        return metalDiskInternals
            .stream()
            .map(MetalDiskInternal::toCoralModel)
            .collect(Collectors.toList());
    }

    public static MetalDisk toCoralModel(
        final MetalDiskInternal metalDiskInternal) {
        return MetalDisk.builder()
            .withDiskServerAddress(metalDiskInternal.getServerAddress())
            .withLogicalDiskId(metalDiskInternal.getLogicalDiskId())
            .build();
    }

    public String getDiskServerIp() {
        return MetalServerInternal.builder()
            .serverAddress(this.serverAddress)
            .build().getIp();
    }
}
