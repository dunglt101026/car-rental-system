package com.hsf.asgm.car;

import java.util.Arrays;
import java.util.Optional;

public enum CarStatus {
    Available,
    Rented,
    Inactive;

    /**
     * Parses a filter value from the request. Unknown or blank values (e.g. "ALL") return empty.
     */
    public static Optional<CarStatus> from(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String trimmed = value.trim();
        return Arrays.stream(values())
                .filter(s -> s.name().equalsIgnoreCase(trimmed))
                .findFirst();
    }
}
