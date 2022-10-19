package com.amazonaws.ebsblacksmithservice.placement;

import java.util.List;
import java.util.Optional;

import com.amazonaws.ebsblacksmithservice.TargetingOptions;

public class TargetingOptionsInternal {

    private List<String> serverIpList;

    public TargetingOptionsInternal(final TargetingOptions targetingOptions) {
        if (targetingOptions != null && targetingOptions.getTargetPrimaryServer() != null) {
            this.serverIpList = targetingOptions.getTargetPrimaryServer().getIpAddresses();
        }
    }

    public Optional<List<String>> getServerIpList() {
        if (this.serverIpList == null || this.serverIpList.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(this.serverIpList);
    }

}
