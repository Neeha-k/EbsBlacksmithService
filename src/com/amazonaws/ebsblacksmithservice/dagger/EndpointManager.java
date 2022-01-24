package com.amazonaws.ebsblacksmithservice.dagger;

import com.amazon.coral.bobcat.BobcatServer;

import java.util.ArrayList;
import java.util.List;

public class EndpointManager {
    private List<BobcatServer> servers;

    EndpointManager(List<BobcatServer> servers) {
        this.servers = new ArrayList<>(servers);
    }

    public void start() throws Exception {
        for (BobcatServer server : servers) {
            server.start();
        }
    }

    public void shutdown() {
        for (BobcatServer server: servers) {
            server.shutdown();
        }
    }
}
