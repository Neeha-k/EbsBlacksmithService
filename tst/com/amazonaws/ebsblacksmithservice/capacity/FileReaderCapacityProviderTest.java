package com.amazonaws.ebsblacksmithservice.capacity;

import com.amazonaws.ebsblacksmithservice.capacity.FileReaderCapacityProvider;
import com.amazonaws.ebsblacksmithservice.types.MetalServerInternal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class FileReaderCapacityProviderTest {

    @Test
    void testFileSingleServerDeserializedProperly() {
        FileReaderCapacityProvider capacityProvider = new FileReaderCapacityProvider("tst/data/single-server.json");
        List<MetalServerInternal> metalServerInternals = capacityProvider.loadServerData();

        Assertions.assertEquals(1, metalServerInternals.size());
        Assertions.assertEquals("127.0.1.0", metalServerInternals.get(0).getIpAddress());
        Assertions.assertEquals(0, metalServerInternals.get(0).getAvailableDisks());
    }

    @Test
    void testFileMultipleServersDeserializesProperly() {
        FileReaderCapacityProvider capacityProvider = new FileReaderCapacityProvider("tst/data/multiple-servers.json");
        List<MetalServerInternal> metalServerInternals = capacityProvider.loadServerData();

        Assertions.assertEquals(2, metalServerInternals.size());
        Assertions.assertEquals("127.0.1.0", metalServerInternals.get(0).getIpAddress());
        Assertions.assertEquals(0, metalServerInternals.get(0).getAvailableDisks());
        Assertions.assertEquals("127.0.1.1", metalServerInternals.get(1).getIpAddress());
        Assertions.assertEquals(1, metalServerInternals.get(1).getAvailableDisks());
    }
}
