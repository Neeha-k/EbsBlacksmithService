package com.amazonaws.ebsblacksmithservice.health;

import com.amazon.coral.service.HealthCheckStrategy;

public class DeepHealthCheck implements HealthCheckStrategy {

    /**
     * Performs a deep health check on your service.
     *
     * You should replace this method with some simple but meaningful health
     * check. This will be invoked at startup during SanityTest to make
     * sure you have got everything configured properly before adding this
     * host to the VIP.
     *
     * @return true if the service is healthy
     */
    public boolean isHealthy()
    {
       // Helpful checks to include here:
       // - Checks that all remote services and stores
       // - Checks that involve config unique to each stage

        return true;
    }

}

