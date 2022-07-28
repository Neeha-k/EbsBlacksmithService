package com.amazonaws.ebsblacksmithservice.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.experimental.UtilityClass;

import com.amazonaws.ebsblacksmithservice.types.MetalDiskInternal;
import com.amazonaws.ebsblacksmithservice.types.MetalServerInternal;

@UtilityClass
public class TestDataGenerator {

    public static List<MetalServerInternal> generateMetalServers(final int serverCount) {
        List<MetalServerInternal> servers = new ArrayList<>();
        for (int i = 0; i < serverCount; i++) {
            servers.add(generateMetalServerWithRandomData());
        }
        return servers;
    }

    private static MetalServerInternal generateMetalServerWithRandomData() {
        return MetalServerInternal
            .builder()
            .serverAddress(UUID.randomUUID().toString())
            .build();
    }

    public static List<MetalDiskInternal> generateMetalDisks(final int diskCount) {
        final List<MetalDiskInternal> disks = new ArrayList<>();
        for (int i = 0; i < diskCount; i++) {
            disks.add(generateMetalDiskWithRandomData());
        }
        return disks;
    }

    private static MetalDiskInternal generateMetalDiskWithRandomData() {
        return MetalDiskInternal
            .builder()
            .logicalDiskId(UUID.randomUUID().toString())
            .serverAddress(UUID.randomUUID().toString())
            .build();
    }
}
