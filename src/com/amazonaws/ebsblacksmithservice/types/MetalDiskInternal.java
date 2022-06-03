package com.amazonaws.ebsblacksmithservice.types;

import com.amazon.aws.authruntimeclient.internal.collections4.ListUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents the Disk entity in the context of BlackSmith service. Attributes of disks in this class
 * contain (in-future will contain) all the information retrieved from Data-plane and needed for identifying the
 * placement strategy.
 */
@Getter
@Builder
@Jacksonized
public class MetalDiskInternal {

    private final String logicalDiskId;
    private final DiskStatus status;

    public static List<MetalDiskInternal> deepCopy(
            final List<MetalDiskInternal> metalDisks) {
        return ListUtils.emptyIfNull(metalDisks)
                .stream()
                .map(disk -> MetalDiskInternal.builder()
                        .logicalDiskId(disk.getLogicalDiskId())
                        .status(disk.getStatus())
                        .build())
                .collect(Collectors.toList());
    }
}
