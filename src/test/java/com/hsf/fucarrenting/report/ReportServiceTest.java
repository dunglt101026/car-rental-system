package com.hsf.fucarrenting.report;

import com.hsf.fucarrenting.car.entity.Car;
import com.hsf.fucarrenting.car.entity.CarStatus;
import com.hsf.fucarrenting.customer.entity.Customer;
import com.hsf.fucarrenting.rental.entity.CarRental;
import com.hsf.fucarrenting.rental.repository.CarRentalRepository;
import com.hsf.fucarrenting.rental.mapper.RentalMapper;
import com.hsf.fucarrenting.rental.dto.RentalResponse;
import com.hsf.fucarrenting.rental.entity.RentalStatus;
import com.hsf.fucarrenting.report.dto.RentalReportResponse;
import com.hsf.fucarrenting.report.service.ReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private CarRentalRepository carRentalRepository;

    @Mock
    private RentalMapper rentalMapper;

    @InjectMocks
    private ReportServiceImpl reportService;

    private Car testCar;
    private Customer testCustomer;
    private CarRental testRental;

    @BeforeEach
    void setUp() {
        testCar = Car.builder()
                .carId(1)
                .carName("Toyota Corolla")
                .rentPrice(BigDecimal.valueOf(1000000))
                .status(CarStatus.Rented)
                .build();

        testCustomer = Customer.builder()
                .customerId(1)
                .fullName("Nguyễn Văn A")
                .mobile("0912345678")
                .build();

        testRental = CarRental.builder()
                .carRentId(10)
                .customer(testCustomer)
                .car(testCar)
                .pickupDate(LocalDate.now())
                .returnDate(LocalDate.now().plusDays(3))
                .rentPrice(BigDecimal.valueOf(3000000))
                .status(RentalStatus.Active)
                .build();
    }

    @Test
    @DisplayName("generateReport - Tạo báo cáo doanh thu sắp xếp theo ngày nhận xe DESC")
    void testGenerateReport_SortByDate_Success() {
        LocalDate start = LocalDate.now().minusDays(10);
        LocalDate end = LocalDate.now();

        CarRental rental2 = CarRental.builder()
                .carRentId(11)
                .customer(testCustomer)
                .car(testCar)
                .pickupDate(LocalDate.now().minusDays(2))
                .returnDate(LocalDate.now().plusDays(1))
                .rentPrice(BigDecimal.valueOf(2000000))
                .status(RentalStatus.Completed)
                .build();

        when(carRentalRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of(testRental, rental2));

        RentalResponse resp1 = new RentalResponse(
                10, 1, "Nguyễn Văn A", "0912345678", "a@test.com",
                1, "Toyota Corolla", "Toyota",
                LocalDate.now(), LocalDate.now().plusDays(3),
                BigDecimal.valueOf(3000000), "Active", null, null
        );
        RentalResponse resp2 = new RentalResponse(
                11, 1, "Nguyễn Văn A", "0912345678", "a@test.com",
                1, "Toyota Corolla", "Toyota",
                LocalDate.now().minusDays(2), LocalDate.now().plusDays(1),
                BigDecimal.valueOf(2000000), "Completed", null, null
        );
        when(rentalMapper.toResponse(testRental)).thenReturn(resp1);
        when(rentalMapper.toResponse(rental2)).thenReturn(resp2);

        RentalReportResponse report = reportService.generateReport(start, end, "date");

        assertThat(report.totalRentals()).isEqualTo(2);
        assertThat(report.totalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(5000000));
        assertThat(report.completedRevenue()).isEqualByComparingTo(BigDecimal.valueOf(2000000));
        assertThat(report.activeCount()).isEqualTo(1);
        assertThat(report.completedCount()).isEqualTo(1);
        assertThat(report.sortBy()).isEqualTo("date");
    }

    @Test
    @DisplayName("generateReport - Đơn Cancelled không được cộng vào tổng doanh thu")
    void testGenerateReport_ExcludesCancelledFromRevenue() {
        CarRental cancelled = CarRental.builder()
                .carRentId(12)
                .customer(testCustomer)
                .car(testCar)
                .pickupDate(LocalDate.now().minusDays(1))
                .returnDate(LocalDate.now().plusDays(2))
                .rentPrice(BigDecimal.valueOf(4000000))
                .status(RentalStatus.Cancelled)
                .build();

        when(carRentalRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of(testRental, cancelled));

        RentalReportResponse report = reportService.generateReport(null, null, "revenue");

        assertThat(report.totalRentals()).isEqualTo(2);
        assertThat(report.totalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(3000000));
        assertThat(report.cancelledCount()).isEqualTo(1);
        assertThat(report.sortBy()).isEqualTo("revenue");
    }
}
