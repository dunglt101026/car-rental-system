package com.hsf.fucarrenting.report;

import java.time.LocalDate;

public interface ReportService {

    /**
     * Builds the rental statistics for the period, sorted in descending order by "date" (pickup date)
     * or "revenue". Missing dates default to the last 30 days; reversed dates are swapped.
     */
    RentalReportResponse generateReport(LocalDate startDate, LocalDate endDate, String sortBy);
}
