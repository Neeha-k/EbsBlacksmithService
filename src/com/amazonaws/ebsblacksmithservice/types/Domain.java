package com.amazonaws.ebsblacksmithservice.types;

public enum Domain {
    DESKTOP,
    EC2,
    GAMMA,
    PROD;

    public static Domain fromString(String domain) {
        return Domain.valueOf(domain.toUpperCase());
    }

    public String toString() {
        return name().toLowerCase();
    }

}
