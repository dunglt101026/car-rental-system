package com.hsf.asgm.car;

import com.hsf.asgm.common.persistence.CaseInsensitiveEnumConverter;
import jakarta.persistence.Converter;

@Converter
public class CarStatusConverter extends CaseInsensitiveEnumConverter<CarStatus> {
    public CarStatusConverter() {
        super(CarStatus.class);
    }
}
