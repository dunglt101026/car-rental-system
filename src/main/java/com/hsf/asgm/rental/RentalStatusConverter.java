package com.hsf.asgm.rental;

import com.hsf.asgm.common.persistence.CaseInsensitiveEnumConverter;
import jakarta.persistence.Converter;

@Converter
public class RentalStatusConverter extends CaseInsensitiveEnumConverter<RentalStatus> {
    public RentalStatusConverter() {
        super(RentalStatus.class);
    }
}
