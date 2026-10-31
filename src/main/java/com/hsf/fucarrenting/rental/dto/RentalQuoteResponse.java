package com.hsf.fucarrenting.rental.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Estimated cost of a booking before it is confirmed: one line per car, same period for all cars.
 */
public record RentalQuoteResponse(
        long days,
        List<Line> lines,
        BigDecimal totalPrice
) {
    public record Line(
            Integer carId,
            String carName,
            BigDecimal dailyPrice,
            BigDecimal subtotal
    ) {}
}
