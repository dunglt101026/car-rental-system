package com.hsf.fucarrenting.report.service;

import com.hsf.fucarrenting.rental.entity.CarRental;
import com.hsf.fucarrenting.rental.repository.CarRentalRepository;
import com.hsf.fucarrenting.rental.mapper.RentalMapper;
import com.hsf.fucarrenting.rental.dto.RentalResponse;
import com.hsf.fucarrenting.rental.repository.RentalSpecification;
import com.hsf.fucarrenting.rental.entity.RentalStatus;
import com.hsf.fucarrenting.report.dto.RentalReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final CarRentalRepository carRentalRepository;
    private final RentalMapper rentalMapper;

    @Override
    public RentalReportResponse generateReport(LocalDate startDate, LocalDate endDate, String sortBy) {
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate.isAfter(endDate)) {
            LocalDate temp = startDate;
            startDate = endDate;
            endDate = temp;
        }

        String validSortBy = "revenue".equalsIgnoreCase(sortBy) ? "revenue" : "date";

        Sort sort = "revenue".equalsIgnoreCase(validSortBy)
                ? Sort.by(Sort.Direction.DESC, "rentPrice").and(Sort.by(Sort.Direction.DESC, "carRentId"))
                : Sort.by(Sort.Direction.DESC, "pickupDate").and(Sort.by(Sort.Direction.DESC, "carRentId"));

        Specification<CarRental> spec = RentalSpecification.filterReport(startDate, endDate);
        List<CarRental> rentals = carRentalRepository.findAll(spec, sort);

        List<RentalResponse> rentalResponses = rentals.stream()
                .map(rental -> rentalMapper.toResponse(rental))
                .toList();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal completedRevenue = BigDecimal.ZERO;
        long activeCount = 0;
        long completedCount = 0;
        long cancelledCount = 0;

        for (CarRental r : rentals) {
            // Cancelled rentals generate no revenue
            if (r.getRentPrice() != null && r.getStatus() != RentalStatus.Cancelled) {
                totalRevenue = totalRevenue.add(r.getRentPrice());
                if (r.getStatus() == RentalStatus.Completed) {
                    completedRevenue = completedRevenue.add(r.getRentPrice());
                }
            }
            if (r.getStatus() == RentalStatus.Active) {
                activeCount++;
            } else if (r.getStatus() == RentalStatus.Completed) {
                completedCount++;
            } else if (r.getStatus() == RentalStatus.Cancelled) {
                cancelledCount++;
            }
        }

        return new RentalReportResponse(
                startDate,
                endDate,
                validSortBy,
                rentalResponses.size(),
                totalRevenue,
                completedRevenue,
                activeCount,
                completedCount,
                cancelledCount,
                rentalResponses
        );
    }
}
