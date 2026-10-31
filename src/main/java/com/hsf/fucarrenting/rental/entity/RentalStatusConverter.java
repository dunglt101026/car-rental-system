package com.hsf.fucarrenting.rental.entity;

import com.hsf.fucarrenting.common.persistence.CaseInsensitiveEnumConverter;
import jakarta.persistence.Converter;

@Converter
public class RentalStatusConverter extends CaseInsensitiveEnumConverter<RentalStatus> {
    public RentalStatusConverter() {
        super(RentalStatus.class);
    }
}
