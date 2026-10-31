package com.hsf.fucarrenting.common.persistence;

import jakarta.persistence.AttributeConverter;

import java.util.Arrays;

/**
 * Maps an enum to its name in the database, reading values case-insensitively and ignoring
 * surrounding spaces. The schema has no CHECK constraint on these columns, so data typed
 * directly into the database (e.g. "available") must not break every page that loads it.
 */
public abstract class CaseInsensitiveEnumConverter<E extends Enum<E>> implements AttributeConverter<E, String> {

    private final Class<E> type;

    protected CaseInsensitiveEnumConverter(Class<E> type) {
        this.type = type;
    }

    @Override
    public String convertToDatabaseColumn(E value) {
        return value == null ? null : value.name();
    }

    @Override
    public E convertToEntityAttribute(String dbValue) {
        if (dbValue == null) {
            return null;
        }
        String trimmed = dbValue.trim();
        return Arrays.stream(type.getEnumConstants())
                .filter(e -> e.name().equalsIgnoreCase(trimmed))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Giá trị '" + dbValue + "' trong DB không hợp lệ cho " + type.getSimpleName()));
    }
}
