package com.hsf.fucarrenting.report;

import com.hsf.fucarrenting.rental.RentalResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RentalReportResponse(
        LocalDate startDate,
        LocalDate endDate,
        String sortBy,
        long totalRentals,
        BigDecimal totalRevenue,
        BigDecimal completedRevenue,
        long activeCount,
        long completedCount,
        long cancelledCount,
        List<RentalResponse> rentals
) {}

