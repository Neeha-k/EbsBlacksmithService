package com.amazonaws.ebsblacksmithservice.capacity;

import com.amazonaws.ebsblacksmithservice.types.MetalServerInternal;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
public class FileReaderCapacityProvider implements CapacityProvider {
    private final String fileLocation;
    private final ObjectMapper objectMapper;

    public FileReaderCapacityProvider(String fileLocation) {
        this.fileLocation = fileLocation;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<MetalServerInternal> loadServerData() {
        log.info(String.format("Loading metal server data from file: %s", this.fileLocation));
        try (InputStream file = new FileInputStream(fileLocation)) {
            return objectMapper.readValue(file, new TypeReference<>(){});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
