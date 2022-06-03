package com.amazonaws.ebsblacksmithservice.types;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum DiskStatus {

    UNPROVISIONED("Unprovisioned"),
    AVAILABLE("Available"),
    MISSING("Missing"),
    ALLOCATED("Allocated"),
    ALLOCATED_MISSING("Allocated_Missing"),
    LEASED("Leased"),
    LEASED_MISSING("Leased_Missing");

    private final String statusName;

    @JsonValue
    public String getStatusName() {
        return statusName;
    }
}
