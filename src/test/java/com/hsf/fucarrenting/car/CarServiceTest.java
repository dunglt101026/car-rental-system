package com.hsf.fucarrenting.car;

import com.hsf.fucarrenting.common.exception.BusinessException;
import com.hsf.fucarrenting.common.exception.ResourceNotFoundException;
import com.hsf.fucarrenting.rental.CarRentalRepository;
import com.hsf.fucarrenting.rental.RentalStatus;
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
class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private CarProducerRepository carProducerRepository;

    @Mock
    private CarRentalRepository carRentalRepository;

    @Mock
    private CarMapper carMapper;

    @InjectMocks
    private CarServiceImpl carService;

    private CarProducer testProducer;
    private Car testCar;

    @BeforeEach
    void setUp() {
        testProducer = CarProducer.builder()
                .producerId(1)
                .producerName("Toyota")
                .address("Tokyo, Japan")
                .country("Japan")
                .build();

        testCar = Car.builder()
                .carId(10)
                .carName("Toyota Vios 1.5G")
                .carModelYear(2023)
                .color("Trắng")
                .capacity(5)
                .description("Xe gia đình tiết kiệm nhiên liệu")
                .importDate(LocalDate.of(2023, 1, 15))
                .producer(testProducer)
                .rentPrice(BigDecimal.valueOf(800000))
                .status(CarStatus.Available)
                .build();
    }

    @Test
    @DisplayName("deleteCar - Khi xe CHƯA từng có đơn thuê -> Xóa vật lý khỏi DB")
    void testDeleteCar_WhenNoRentalHistory_ShouldHardDelete() {
        when(carRepository.findById(10)).thenReturn(Optional.of(testCar));
        when(carRentalRepository.existsByCar_CarId(10)).thenReturn(false);

        CarDeleteResult result = carService.deleteCar(10);

        assertThat(result.hardDeleted()).isTrue();
        assertThat(result.carId()).isEqualTo(10);
        assertThat(result.carName()).isEqualTo("Toyota Vios 1.5G");
        verify(carRepository, times(1)).delete(testCar);
        verify(carRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteCar - Khi xe ĐÃ từng có đơn thuê -> Chuyển status sang Inactive (Soft Delete)")
    void testDeleteCar_WhenHasRentalHistory_ShouldSoftDeleteToInactive() {
        when(carRepository.findById(10)).thenReturn(Optional.of(testCar));
        when(carRentalRepository.existsByCar_CarId(10)).thenReturn(true);

        CarDeleteResult result = carService.deleteCar(10);

        assertThat(result.hardDeleted()).isFalse();
        assertThat(result.carId()).isEqualTo(10);
        assertThat(testCar.getStatus()).isEqualTo(CarStatus.Inactive);
        verify(carRepository, times(1)).save(testCar);
        verify(carRepository, never()).delete(any(Car.class));
    }

    @Test
    @DisplayName("deleteCar - Xe không tồn tại -> Ném ResourceNotFoundException")
    void testDeleteCar_WhenCarNotFound_ShouldThrowException() {
        when(carRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carService.deleteCar(999))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("createCar - Thêm mới xe thành công với nhà sản xuất hợp lệ")
    void testCreateCar_Success() {
        CarRequest request = new CarRequest(
                "Honda CR-V",
                2024,
                "Đen",
                7,
                "SUV đa dụng",
                LocalDate.now(),
                1,
                BigDecimal.valueOf(1200000),
                CarStatus.Available.name()
        );

        when(carProducerRepository.findById(1)).thenReturn(Optional.of(testProducer));
        when(carMapper.toEntity(request)).thenReturn(Car.builder().carName(request.carName()).build());
        when(carRepository.save(any(Car.class))).thenAnswer(invocation -> {
            Car c = invocation.getArgument(0);
            c.setCarId(100);
            return c;
        });

        CarResponse mockResponse = new CarResponse(
                100, "Honda CR-V", 2024, "Đen", 7, "SUV đa dụng",
                LocalDate.now(), 1, "Toyota", BigDecimal.valueOf(1200000), "Available"
        );
        when(carMapper.toResponse(any(Car.class))).thenReturn(mockResponse);

        CarResponse response = carService.createCar(request);

        assertThat(response).isNotNull();
        assertThat(response.carId()).isEqualTo(100);
        assertThat(response.carName()).isEqualTo("Honda CR-V");
        verify(carRepository, times(1)).save(any(Car.class));
    }

    @Test
    @DisplayName("createCar - Thêm xe thất bại khi nhà sản xuất không tồn tại")
    void testCreateCar_ProducerNotFound_ShouldThrowException() {
        CarRequest request = new CarRequest(
                "Honda CR-V", 2024, "Đen", 7, "SUV",
                LocalDate.now(), 999, BigDecimal.valueOf(1200000), "Available"
        );

        when(carProducerRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> carService.createCar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Không tìm thấy nhà sản xuất");
    }

    @Test
    @DisplayName("updateCar - Cập nhật thông tin xe thành công")
    void testUpdateCar_Success() {
        CarRequest request = new CarRequest(
                "Toyota Vios 1.5G 2024 Update",
                2024,
                "Bạc",
                5,
                "Cập nhật mới",
                LocalDate.of(2023, 1, 15),
                1,
                BigDecimal.valueOf(850000),
                CarStatus.Available.name()
        );

        when(carRepository.findById(10)).thenReturn(Optional.of(testCar));
        when(carProducerRepository.findById(1)).thenReturn(Optional.of(testProducer));
        doAnswer(invocation -> {
            testCar.setCarName(request.carName());
            testCar.setColor(request.color());
            testCar.setRentPrice(request.rentPrice());
            return null;
        }).when(carMapper).updateEntityFromRequest(request, testCar);
        when(carRepository.save(any(Car.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CarResponse mockResponse = new CarResponse(
                10, "Toyota Vios 1.5G 2024 Update", 2024, "Bạc", 5, "Cập nhật mới",
                LocalDate.of(2023, 1, 15), 1, "Toyota", BigDecimal.valueOf(850000), "Available"
        );
        when(carMapper.toResponse(any(Car.class))).thenReturn(mockResponse);

        CarResponse response = carService.updateCar(10, request);

        assertThat(response.carName()).isEqualTo("Toyota Vios 1.5G 2024 Update");
        assertThat(testCar.getRentPrice()).isEqualTo(BigDecimal.valueOf(850000));
        assertThat(testCar.getColor()).isEqualTo("Bạc");
    }

    @Test
    @DisplayName("getAdminCars - Lấy danh sách phân trang thành công")
    void testGetAdminCars_Success() {
        Page<Car> carPage = new PageImpl<>(List.of(testCar));
        when(carRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(carPage);

        CarResponse mockResponse = new CarResponse(
                10, "Toyota Vios 1.5G", 2023, "Trắng", 5, "Xe gia đình",
                LocalDate.of(2023, 1, 15), 1, "Toyota", BigDecimal.valueOf(800000), "Available"
        );
        when(carMapper.toResponse(testCar)).thenReturn(mockResponse);

        Page<CarResponse> result = carService.getAdminCars("Vios", 1, "Available", 0, 10);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).carName()).isEqualTo("Toyota Vios 1.5G");
    }

    private CarRequest requestWithStatus(CarStatus status) {
        return new CarRequest("Toyota Vios 1.5G", 2024, "Bạc", 5, "Mô tả",
                LocalDate.of(2023, 1, 15), 1, BigDecimal.valueOf(850000), status.name());
    }

    @Test
    @DisplayName("updateCar - Xe đang có đơn Active -> Không cho đổi sang Available")
    void testUpdateCar_ActiveRentalToAvailable_ShouldThrowException() {
        when(carRepository.findById(10)).thenReturn(Optional.of(testCar));
        when(carProducerRepository.findById(1)).thenReturn(Optional.of(testProducer));
        when(carRentalRepository.existsByCar_CarIdAndStatus(10, RentalStatus.Active)).thenReturn(true);

        assertThatThrownBy(() -> carService.updateCar(10, requestWithStatus(CarStatus.Available)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("đang có đơn thuê Active");
        verify(carRepository, never()).save(any(Car.class));
    }

    @Test
    @DisplayName("updateCar - Xe không có đơn Active -> Không cho đặt tay sang Rented")
    void testUpdateCar_NoActiveRentalToRented_ShouldThrowException() {
        when(carRepository.findById(10)).thenReturn(Optional.of(testCar));
        when(carProducerRepository.findById(1)).thenReturn(Optional.of(testProducer));
        when(carRentalRepository.existsByCar_CarIdAndStatus(10, RentalStatus.Active)).thenReturn(false);

        assertThatThrownBy(() -> carService.updateCar(10, requestWithStatus(CarStatus.Rented)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("không thể đặt trạng thái Rented");
        verify(carRepository, never()).save(any(Car.class));
    }

    @Test
    @DisplayName("createCar - Không cho tạo xe ở trạng thái Rented")
    void testCreateCar_WithRentedStatus_ShouldThrowException() {
        when(carProducerRepository.findById(1)).thenReturn(Optional.of(testProducer));

        assertThatThrownBy(() -> carService.createCar(requestWithStatus(CarStatus.Rented)))
                .isInstanceOf(BusinessException.class);
        verify(carRepository, never()).save(any(Car.class));
    }

    @Test
    @DisplayName("deleteCar - Xe đang được thuê (đơn Active) -> Không cho xóa")
    void testDeleteCar_WhenActiveRental_ShouldThrowException() {
        when(carRepository.findById(10)).thenReturn(Optional.of(testCar));
        when(carRentalRepository.existsByCar_CarIdAndStatus(10, RentalStatus.Active)).thenReturn(true);

        assertThatThrownBy(() -> carService.deleteCar(10))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("đang được thuê");
        verify(carRepository, never()).save(any(Car.class));
        verify(carRepository, never()).delete(any(Car.class));
    }
}
