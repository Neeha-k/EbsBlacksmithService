package com.amazonaws.ebsblacksmithservice.capacity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.amazonaws.ebsblacksmithservice.types.MetalDiskInternal;
import com.amazonaws.ebsblacksmithservice.types.MetalServerInternal;

public class FileReaderCapacityProviderTest {

    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testFileSingleServerDeserializedProperly() {
        final FileReaderCapacityProvider capacityProvider =
            new FileReaderCapacityProvider(
                "tst/data/single-server.json",
                "tst/data/single-disk.json",
                objectMapper);

        final List<MetalServerInternal> metalServerInternals = capacityProvider.loadServerData();
        final List<MetalDiskInternal> metalDiskInternals = capacityProvider.loadDiskData();

        assertNotNull(metalServerInternals);
        assertEquals(1, metalServerInternals.size());
        assertEquals("127.0.1.0:5684", metalServerInternals.get(0).getServerAddress());

        assertNotNull(metalDiskInternals);
        assertEquals(1, metalDiskInternals.size());
        assertEquals("127.0.1.0:5684", metalDiskInternals.get(0).getServerAddress());
        assertEquals("3FJM539T001", metalDiskInternals.get(0).getLogicalDiskId());
    }

    @Test
    void testFileMultipleServersDeserializesProperly() {
        final FileReaderCapacityProvider capacityProvider =
            new FileReaderCapacityProvider(
                "tst/data/multiple-servers.json",
                "tst/data/multiple-disk.json",
                objectMapper);

        final List<MetalServerInternal> metalServerInternals = capacityProvider.loadServerData();
        final List<MetalDiskInternal> metalDiskInternals = capacityProvider.loadDiskData();

        assertNotNull(metalServerInternals);
        assertEquals(2, metalServerInternals.size());
        assertEquals("127.0.1.0:5684", metalServerInternals.get(0).getServerAddress());
        assertEquals("127.0.1.1:5684", metalServerInternals.get(1).getServerAddress());

        assertNotNull(metalDiskInternals);
        assertEquals(2, metalDiskInternals.size());
        assertEquals("127.0.1.0:5684", metalDiskInternals.get(0).getServerAddress());
        assertEquals("3FJM539T001", metalDiskInternals.get(0).getLogicalDiskId());
        assertEquals("127.0.1.1:5684", metalDiskInternals.get(1).getServerAddress());
        assertEquals("3FJM539T002", metalDiskInternals.get(1).getLogicalDiskId());
    }
}
