package com.amazonaws.ebsblacksmithservice.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import lombok.experimental.UtilityClass;

import com.amazonaws.ebsblacksmithservice.types.MetalDiskInternal;
import com.amazonaws.ebsblacksmithservice.types.MetalServerInternal;

@UtilityClass
public class TestDataGenerator {

    public static final String DOT = ".";
    public static final String COLON = ":";

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
            .serverAddress(generateRandomServerAddress())
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
            .serverAddress(generateRandomServerAddress())
            .build();
    }

    public static String generateRandomServerAddress() {
        Random random = new Random();
        return String.format("%d%s%d%s%d%s%d%s%d",
            random.nextInt(256),
            DOT,
            random.nextInt(256),
            DOT,
            random.nextInt(256),
            DOT,
            random.nextInt(256),
            COLON,
            random.nextInt(9999));
    }
}
