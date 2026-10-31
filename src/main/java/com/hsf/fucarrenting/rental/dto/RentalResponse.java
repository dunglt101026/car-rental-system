package com.hsf.fucarrenting.rental.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RentalResponse(
        Integer carRentId,
        Integer customerId,
        String customerFullName,
        String customerMobile,
        String customerEmail,
        Integer carId,
        String carName,
        String carProducerName,
        LocalDate pickupDate,
        LocalDate returnDate,
        BigDecimal rentPrice,
        String status,
        Integer reviewStar,
        String reviewComment
) {}
