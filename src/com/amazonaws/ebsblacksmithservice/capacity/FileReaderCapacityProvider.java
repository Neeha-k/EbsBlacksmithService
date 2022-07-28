package com.amazonaws.ebsblacksmithservice.capacity;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.amazonaws.ebsblacksmithservice.types.MetalDiskInternal;
import com.amazonaws.ebsblacksmithservice.types.MetalServerInternal;

@Slf4j
@AllArgsConstructor
public class FileReaderCapacityProvider implements CapacityProvider {
    private final String serverFileLocation;
    private final String diskFileLocation;
    private final ObjectMapper objectMapper;

    @Override
    public List<MetalServerInternal> loadServerData() {
        log.info("Loading metal server data from file: {}", this.serverFileLocation);
        return loadDataFromFile(this.serverFileLocation, MetalServerInternal.class);
    }

    @Override
    public List<MetalDiskInternal> loadDiskData() {
        log.info("Loading metal disk data from file: {}", this.diskFileLocation);
        return loadDataFromFile(this.diskFileLocation, MetalDiskInternal.class);
    }

    private <T> List<T> loadDataFromFile(
        final String fileLocation,
        final Class<T> clazz) {

        try (final InputStream file = new FileInputStream(fileLocation)) {
            return objectMapper.readValue(
                file,
                objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (final IOException e) {
            log.error("Failed to read file from: {} into array of object type: {}", fileLocation, clazz.getSimpleName());
            throw new RuntimeException(e);
        }
    }
}
