package com.hsf.fucarrenting.car;

import com.hsf.fucarrenting.common.persistence.CaseInsensitiveEnumConverter;
import jakarta.persistence.Converter;

@Converter
public class CarStatusConverter extends CaseInsensitiveEnumConverter<CarStatus> {
    public CarStatusConverter() {
        super(CarStatus.class);
    }
}
