package com.hsf.asgm.rental;

import java.util.Arrays;
import java.util.Optional;

public enum RentalStatus {
    Active,
    Completed,
    Cancelled;

    /**
     * Parses a filter value from the request. Unknown or blank values (e.g. "ALL") return empty.
     */
    public static Optional<RentalStatus> from(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String trimmed = value.trim();
        return Arrays.stream(values())
                .filter(s -> s.name().equalsIgnoreCase(trimmed))
                .findFirst();
    }
}
