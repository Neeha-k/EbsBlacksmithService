package com.amazonaws.ebsblacksmithservice.capacity;

import com.amazonaws.ebsblacksmithservice.types.MetalServerInternal;

import java.util.List;

public interface CapacityProvider {
    List<MetalServerInternal> loadServerData();
}
