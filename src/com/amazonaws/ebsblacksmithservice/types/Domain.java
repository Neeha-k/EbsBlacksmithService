package com.amazonaws.ebsblacksmithservice.types;

import com.google.common.collect.ImmutableList;

public enum Domain {
    DESKTOP,
    EC2, /* EC2 (IDM) */
    GAMMA,
    PROD;

    // TODO: We should add IDM to SECURE_DOMAINS when we are ready to fully support secure endpoints
    private static final ImmutableList<Domain> SECURE_DOMAINS = ImmutableList.of(GAMMA, PROD);

    public static Domain fromString(String domain) {
        return Domain.valueOf(domain.toUpperCase());
    }

    public String toString() {
        return name().toLowerCase();
    }

    public static boolean isSecureDomain(Domain domain) {
        return SECURE_DOMAINS.contains(domain);
    }
}
