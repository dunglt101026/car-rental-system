package com.hsf.asgm.rental;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RentalResponse(
        Integer carRentId,
        Integer customerId,
        String customerFullName,
        String customerMobile,
        Integer carId,
        String carName,
        LocalDate pickupDate,
        LocalDate returnDate,
        BigDecimal rentPrice,
        String status,
        Integer reviewStar,
        String reviewComment
) {}

