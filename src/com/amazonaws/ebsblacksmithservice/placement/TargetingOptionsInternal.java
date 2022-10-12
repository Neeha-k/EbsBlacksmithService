package com.amazonaws.ebsblacksmithservice.placement;

import java.util.Optional;

import com.amazonaws.ebsblacksmithservice.TargetingOptions;

public class TargetingOptionsInternal {

    private String serverIp;

    public TargetingOptionsInternal(final TargetingOptions targetingOptions) {
        if (null != targetingOptions) {
            this.serverIp = targetingOptions.getTargetServerIp();
        }
    }

    public Optional<String> getServerIp() {
        return Optional.ofNullable(this.serverIp);
    }
}
