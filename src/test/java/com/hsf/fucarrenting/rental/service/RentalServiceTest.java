package com.hsf.fucarrenting.rental.service;

import com.hsf.fucarrenting.car.entity.Car;
import com.hsf.fucarrenting.car.repository.CarRepository;
import com.hsf.fucarrenting.car.entity.CarStatus;
import com.hsf.fucarrenting.common.exception.BusinessException;
import com.hsf.fucarrenting.customer.entity.Customer;
import com.hsf.fucarrenting.customer.repository.CustomerRepository;
import com.hsf.fucarrenting.rental.dto.RentalCreateRequest;
import com.hsf.fucarrenting.rental.dto.RentalQuoteResponse;
import com.hsf.fucarrenting.rental.dto.RentalResponse;
import com.hsf.fucarrenting.rental.entity.CarRental;
import com.hsf.fucarrenting.rental.entity.RentalStatus;
import com.hsf.fucarrenting.rental.mapper.RentalMapper;
import com.hsf.fucarrenting.rental.repository.CarRentalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
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
    private RentalServiceImpl rentalService;

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
    @DisplayName("getAdminRentals - Phân trang và tìm kiếm đơn thuê thành công")
    void testGetAdminRentals_Success() {
        Page<CarRental> page = new PageImpl<>(List.of(testRental));
        when(carRentalRepository.findAll(ArgumentMatchers.<Specification<CarRental>>any(), any(Pageable.class))).thenReturn(page);

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
        when(carRentalRepository.findWithDetailsByCarRentId(10)).thenReturn(Optional.of(testRental));
        when(carRentalRepository.save(any(CarRental.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RentalResponse mockResponse = new RentalResponse(
                10, 1, "Nguyễn Văn A", "0912345678", "a@test.com",
                1, "Toyota Corolla", "Toyota",
                LocalDate.now(), LocalDate.now().plusDays(3),
                BigDecimal.valueOf(3000000), "Completed", null, null
        );
        when(rentalMapper.toResponse(any(CarRental.class))).thenReturn(mockResponse);

        RentalResponse response = rentalService.updateRentalStatus(10, "Completed");

        assertThat(response.status()).isEqualTo("Completed");

        assertThat(testRental.getStatus()).isEqualTo(RentalStatus.Completed);
        assertThat(testCar.getStatus()).isEqualTo(CarStatus.Available);
        verify(carRepository, times(1)).save(testCar);
        verify(carRentalRepository, times(1)).save(testRental);
    }

    @Test
    @DisplayName("updateRentalStatus - Chuyển sang Cancelled -> Xe tự động về trạng thái Available")
    void testUpdateRentalStatus_ToCancelled_ShouldSetCarToAvailable() {
        when(carRentalRepository.findWithDetailsByCarRentId(10)).thenReturn(Optional.of(testRental));
        when(carRentalRepository.save(any(CarRental.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RentalResponse mockResponse = new RentalResponse(
                10, 1, "Nguyễn Văn A", "0912345678", "a@test.com",
                1, "Toyota Corolla", "Toyota",
                LocalDate.now(), LocalDate.now().plusDays(3),
                BigDecimal.valueOf(3000000), "Cancelled", null, null
        );
        when(rentalMapper.toResponse(any(CarRental.class))).thenReturn(mockResponse);

        RentalResponse response = rentalService.updateRentalStatus(10, "Cancelled");

        assertThat(response.status()).isEqualTo("Cancelled");

        assertThat(testRental.getStatus()).isEqualTo(RentalStatus.Cancelled);
        assertThat(testCar.getStatus()).isEqualTo(CarStatus.Available);
        verify(carRepository, times(1)).save(testCar);
        verify(carRentalRepository, times(1)).save(testRental);
    }

    @Test
    @DisplayName("updateRentalStatus - Trạng thái không hợp lệ -> Ném BusinessException")
    void testUpdateRentalStatus_InvalidStatus_ShouldThrowException() {
        when(carRentalRepository.findWithDetailsByCarRentId(10)).thenReturn(Optional.of(testRental));

        assertThatThrownBy(() -> rentalService.updateRentalStatus(10, "INVALID_STATUS"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Trạng thái đơn thuê không hợp lệ");

        verify(carRepository, never()).save(any());
        verify(carRentalRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateRentalStatus - Đơn Completed không thể mở lại Active -> Ném BusinessException")
    void testUpdateRentalStatus_CompletedToActive_ShouldThrowException() {
        testRental.setStatus(RentalStatus.Completed);
        testCar.setStatus(CarStatus.Rented); // xe đang được thuê bởi đơn khác
        when(carRentalRepository.findWithDetailsByCarRentId(10)).thenReturn(Optional.of(testRental));

        assertThatThrownBy(() -> rentalService.updateRentalStatus(10, "Active"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("đã kết thúc");

        assertThat(testRental.getStatus()).isEqualTo(RentalStatus.Completed);
        verify(carRepository, never()).save(any());
        verify(carRentalRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateRentalStatus - Đơn Cancelled không thể đổi sang Completed -> Ném BusinessException")
    void testUpdateRentalStatus_CancelledToCompleted_ShouldThrowException() {
        testRental.setStatus(RentalStatus.Cancelled);
        testCar.setStatus(CarStatus.Rented);
        when(carRentalRepository.findWithDetailsByCarRentId(10)).thenReturn(Optional.of(testRental));

        assertThatThrownBy(() -> rentalService.updateRentalStatus(10, "Completed"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("đã kết thúc");

        assertThat(testCar.getStatus()).isEqualTo(CarStatus.Rented);
        verify(carRepository, never()).save(any());
        verify(carRentalRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateRentalStatus - Active sang Active -> Ném BusinessException")
    void testUpdateRentalStatus_ActiveToActive_ShouldThrowException() {
        when(carRentalRepository.findWithDetailsByCarRentId(10)).thenReturn(Optional.of(testRental));

        assertThatThrownBy(() -> rentalService.updateRentalStatus(10, "Active"))
                .isInstanceOf(BusinessException.class);

        verify(carRepository, never()).save(any());
        verify(carRentalRepository, never()).save(any());
    }

    @Test
    @DisplayName("bookCars - Đặt nhiều xe cùng lúc -> Mỗi xe một đơn Active, xe chuyển sang Rented")
    void testBookCars_MultipleCars_Success() {
        Car car1 = Car.builder().carId(1).carName("Toyota Corolla")
                .rentPrice(BigDecimal.valueOf(100)).status(CarStatus.Available).build();
        Car car2 = Car.builder().carId(2).carName("Honda City")
                .rentPrice(BigDecimal.valueOf(50)).status(CarStatus.Available).build();
        when(customerRepository.findById(1)).thenReturn(Optional.of(testCustomer));
        when(carRepository.findById(1)).thenReturn(Optional.of(car1));
        when(carRepository.findById(2)).thenReturn(Optional.of(car2));
        when(carRentalRepository.save(any(CarRental.class))).thenAnswer(inv -> inv.getArgument(0));

        RentalCreateRequest request = new RentalCreateRequest(
                List.of(1, 2, 1), LocalDate.now(), LocalDate.now().plusDays(3));
        List<RentalResponse> result = rentalService.bookCars(1, request);

        org.mockito.ArgumentCaptor<CarRental> captor = org.mockito.ArgumentCaptor.forClass(CarRental.class);
        verify(carRentalRepository, times(2)).save(captor.capture());
        assertThat(result).hasSize(2);
        assertThat(captor.getAllValues()).extracting(rental -> rental.getRentPrice())
                .usingElementComparator((a, b) -> a.compareTo(b))
                .containsExactly(BigDecimal.valueOf(300), BigDecimal.valueOf(150));
        assertThat(captor.getAllValues()).allMatch(r -> r.getStatus() == RentalStatus.Active);
        assertThat(car1.getStatus()).isEqualTo(CarStatus.Rented);
        assertThat(car2.getStatus()).isEqualTo(CarStatus.Rented);
    }

    @Test
    @DisplayName("bookCars - Một xe không khả dụng -> Không đặt xe nào")
    void testBookCars_OneCarUnavailable_ShouldBookNothing() {
        Car car1 = Car.builder().carId(1).carName("Toyota Corolla")
                .rentPrice(BigDecimal.valueOf(100)).status(CarStatus.Available).build();
        Car car2 = Car.builder().carId(2).carName("Honda City")
                .rentPrice(BigDecimal.valueOf(50)).status(CarStatus.Rented).build();
        when(customerRepository.findById(1)).thenReturn(Optional.of(testCustomer));
        when(carRepository.findById(1)).thenReturn(Optional.of(car1));
        when(carRepository.findById(2)).thenReturn(Optional.of(car2));

        RentalCreateRequest request = new RentalCreateRequest(
                List.of(1, 2), LocalDate.now(), LocalDate.now().plusDays(1));

        assertThatThrownBy(() -> rentalService.bookCars(1, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Honda City");

        assertThat(car1.getStatus()).isEqualTo(CarStatus.Available);
        verify(carRentalRepository, never()).save(any());
        verify(carRepository, never()).save(any());
    }

    @Test
    @DisplayName("quoteRental - Tính tổng tiền nhiều xe, không lưu gì vào DB")
    void testQuoteRental_MultipleCars_ShouldSumWithoutSaving() {
        Car car1 = Car.builder().carId(1).carName("Toyota Camry")
                .rentPrice(BigDecimal.valueOf(80)).status(CarStatus.Available).build();
        Car car2 = Car.builder().carId(2).carName("Ford Transit")
                .rentPrice(BigDecimal.valueOf(120)).status(CarStatus.Available).build();
        when(carRepository.findById(1)).thenReturn(Optional.of(car1));
        when(carRepository.findById(2)).thenReturn(Optional.of(car2));

        RentalCreateRequest request = new RentalCreateRequest(
                List.of(1, 2), LocalDate.now(), LocalDate.now().plusDays(2));
        RentalQuoteResponse quote = rentalService.quoteRental(request);

        assertThat(quote.days()).isEqualTo(2);
        assertThat(quote.lines()).extracting(line -> line.subtotal())
                .usingElementComparator((a, b) -> a.compareTo(b))
                .containsExactly(BigDecimal.valueOf(160), BigDecimal.valueOf(240));
        assertThat(quote.totalPrice()).isEqualByComparingTo(BigDecimal.valueOf(400));
        assertThat(car1.getStatus()).isEqualTo(CarStatus.Available);
        verify(carRentalRepository, never()).save(any());
        verify(carRepository, never()).save(any());
    }

    @Test
    @DisplayName("quoteRental - Ngày trả trước ngày nhận -> Báo lỗi")
    void testQuoteRental_ReturnBeforePickup_ShouldThrow() {
        RentalCreateRequest request = new RentalCreateRequest(
                List.of(1), LocalDate.now().plusDays(3), LocalDate.now().plusDays(1));

        assertThatThrownBy(() -> rentalService.quoteRental(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Ngày trả xe phải sau ngày nhận xe");
        verify(carRepository, never()).findById(any());
    }


    @Test
    @DisplayName("updateRentalStatus - Chưa đến ngày nhận xe -> Không được xác nhận trả xe, chỉ được hủy")
    void testUpdateRentalStatus_CompleteBeforePickup_ShouldThrowException() {
        testRental.setPickupDate(LocalDate.now().plusDays(2));
        testRental.setReturnDate(LocalDate.now().plusDays(4));
        when(carRentalRepository.findWithDetailsByCarRentId(10)).thenReturn(Optional.of(testRental));

        assertThatThrownBy(() -> rentalService.updateRentalStatus(10, "Completed"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("chưa đến ngày nhận xe");

        assertThat(testRental.getStatus()).isEqualTo(RentalStatus.Active);
        verify(carRentalRepository, never()).save(any());
    }
}
