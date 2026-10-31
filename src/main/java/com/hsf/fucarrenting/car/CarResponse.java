package com.hsf.fucarrenting.car;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CarResponse(
        Integer carId,
        String carName,
        Integer carModelYear,
        String color,
        Integer capacity,
        String description,
        LocalDate importDate,
        Integer producerId,
        String producerName,
        BigDecimal rentPrice,
        String status
) {}

