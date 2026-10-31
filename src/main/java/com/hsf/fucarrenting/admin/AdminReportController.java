package com.hsf.fucarrenting.admin;

import com.hsf.fucarrenting.report.RentalReportResponse;
import com.hsf.fucarrenting.report.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
@Slf4j
public class AdminReportController {

    private final ReportService reportService;

    @GetMapping
    public String showReportPage(
            @RequestParam(name = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(name = "sortBy", defaultValue = "date") String sortBy,
            Model model
    ) {
        RentalReportResponse report = reportService.generateReport(startDate, endDate, sortBy);

        model.addAttribute("report", report);
        model.addAttribute("startDate", report.startDate());
        model.addAttribute("endDate", report.endDate());
        model.addAttribute("sortBy", report.sortBy());
        model.addAttribute("activeTab", "reports");

        return "admin/report/index";
    }
}

