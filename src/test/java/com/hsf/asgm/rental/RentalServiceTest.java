package com.hsf.asgm.rental;

import com.hsf.asgm.car.Car;
import com.hsf.asgm.car.CarRepository;
import com.hsf.asgm.car.CarStatus;
import com.hsf.asgm.common.exception.BusinessException;
import com.hsf.asgm.customer.Customer;
import com.hsf.asgm.customer.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock
    private CarRentalRepository carRentalRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RentalMapper rentalMapper;

    @InjectMocks
    private RentalService rentalService;

    private Car testCar;
    private Customer testCustomer;
    private CarRental testRental;

    @BeforeEach
    void setUp() {
        testCar = Car.builder()
                .carId(1)
                .carName("Toyota Corolla")
                .rentPrice(BigDecimal.valueOf(1000000))
                .status(CarStatus.Rented.name())
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
                .status(RentalStatus.Active.name())
                .build();
    }

    @Test
    @DisplayName("getAdminRentals - Phân trang và tìm kiếm đơn thuê thành công")
    void testGetAdminRentals_Success() {
        Page<CarRental> page = new PageImpl<>(List.of(testRental));
        when(carRentalRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        RentalResponse mockResponse = new RentalResponse(
                10, 1, "Nguyễn Văn A", "0912345678", "a@test.com",
                1, "Toyota Corolla", "Toyota",
                LocalDate.now(), LocalDate.now().plusDays(3),
                BigDecimal.valueOf(3000000), "Active", null, null
        );
        when(rentalMapper.toResponse(testRental)).thenReturn(mockResponse);

        Page<RentalResponse> result = rentalService.getAdminRentals("Toyota", "Active", 0, 10);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).carName()).isEqualTo("Toyota Corolla");
    }

    @Test
    @DisplayName("updateRentalStatus - Chuyển sang Completed -> Xe tự động về trạng thái Available")
    void testUpdateRentalStatus_ToCompleted_ShouldSetCarToAvailable() {
        when(carRentalRepository.findById(10)).thenReturn(Optional.of(testRental));
        when(carRentalRepository.save(any(CarRental.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RentalResponse mockResponse = new RentalResponse(
                10, 1, "Nguyễn Văn A", "0912345678", "a@test.com",
                1, "Toyota Corolla", "Toyota",
                LocalDate.now(), LocalDate.now().plusDays(3),
                BigDecimal.valueOf(3000000), "Completed", null, null
        );
        when(rentalMapper.toResponse(any(CarRental.class))).thenReturn(mockResponse);

        RentalResponse response = rentalService.updateRentalStatus(10, "Completed");

        assertThat(testRental.getStatus()).isEqualTo(RentalStatus.Completed.name());
        assertThat(testCar.getStatus()).isEqualTo(CarStatus.Available.name());
        verify(carRepository, times(1)).save(testCar);
        verify(carRentalRepository, times(1)).save(testRental);
    }

    @Test
    @DisplayName("updateRentalStatus - Chuyển sang Cancelled -> Xe tự động về trạng thái Available")
    void testUpdateRentalStatus_ToCancelled_ShouldSetCarToAvailable() {
        when(carRentalRepository.findById(10)).thenReturn(Optional.of(testRental));
        when(carRentalRepository.save(any(CarRental.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RentalResponse mockResponse = new RentalResponse(
                10, 1, "Nguyễn Văn A", "0912345678", "a@test.com",
                1, "Toyota Corolla", "Toyota",
                LocalDate.now(), LocalDate.now().plusDays(3),
                BigDecimal.valueOf(3000000), "Cancelled", null, null
        );
        when(rentalMapper.toResponse(any(CarRental.class))).thenReturn(mockResponse);

        RentalResponse response = rentalService.updateRentalStatus(10, "Cancelled");

        assertThat(testRental.getStatus()).isEqualTo(RentalStatus.Cancelled.name());
        assertThat(testCar.getStatus()).isEqualTo(CarStatus.Available.name());
        verify(carRepository, times(1)).save(testCar);
        verify(carRentalRepository, times(1)).save(testRental);
    }

    @Test
    @DisplayName("updateRentalStatus - Trạng thái không hợp lệ -> Ném BusinessException")
    void testUpdateRentalStatus_InvalidStatus_ShouldThrowException() {
        when(carRentalRepository.findById(10)).thenReturn(Optional.of(testRental));

        assertThatThrownBy(() -> rentalService.updateRentalStatus(10, "INVALID_STATUS"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Trạng thái đơn thuê không hợp lệ");

        verify(carRepository, never()).save(any());
        verify(carRentalRepository, never()).save(any());
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
                .status(RentalStatus.Completed.name())
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

        RentalReportResponse report = rentalService.generateReport(start, end, "date");

        assertThat(report.totalRentals()).isEqualTo(2);
        assertThat(report.totalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(5000000));
        assertThat(report.completedRevenue()).isEqualByComparingTo(BigDecimal.valueOf(2000000));
        assertThat(report.activeCount()).isEqualTo(1);
        assertThat(report.completedCount()).isEqualTo(1);
        assertThat(report.sortBy()).isEqualTo("date");
    }
}

