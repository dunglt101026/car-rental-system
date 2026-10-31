package com.hsf.fucarrenting.car;

import com.hsf.fucarrenting.common.exception.BusinessException;
import com.hsf.fucarrenting.common.exception.ResourceNotFoundException;
import com.hsf.fucarrenting.rental.CarRentalRepository;
import com.hsf.fucarrenting.rental.RentalStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final CarProducerRepository carProducerRepository;
    private final CarRentalRepository carRentalRepository;
    private final CarMapper carMapper;

    @Override
    public List<CarResponse> getAvailableCars(
            Integer producerId,
            Integer capacity,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String keyword
    ) {
        Specification<Car> spec = CarSpecification.filterCars(
                CarStatus.Available.name(),
                producerId,
                capacity,
                minPrice,
                maxPrice,
                keyword
        );
        return carRepository.findAll(spec)
                .stream()
                .map(car -> carMapper.toResponse(car))
                .toList();
    }

    @Override
    public Page<CarResponse> getAdminCars(
            String keyword,
            Integer producerId,
            String status,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "carId"));
        Specification<Car> spec = CarSpecification.filterCars(
                status,
                producerId,
                null,
                null,
                null,
                keyword
        );
        return carRepository.findAll(spec, pageable).map(car -> carMapper.toResponse(car));
    }

    @Override
    public CarResponse getCarById(Integer carId) {
        Car car = getCarEntity(carId);
        return carMapper.toResponse(car);
    }

    @Override
    public Car getCarEntity(Integer carId) {
        return carRepository.findById(carId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin xe với mã: " + carId));
    }

    @Override
    public CarRequest getCarFormById(Integer carId) {
        Car car = getCarEntity(carId);
        return carMapper.toRequest(car);
    }

    @Override
    public List<CarProducerResponse> getAllProducers() {
        return carProducerRepository.findAllByOrderByProducerNameAsc()
                .stream()
                .map(producer -> carMapper.toProducerResponse(producer))
                .toList();
    }

    @Transactional
    @Override
    public CarResponse createCar(CarRequest request) {
        CarProducer producer = carProducerRepository.findById(request.producerId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy nhà sản xuất với mã: " + request.producerId()));

        if (CarStatus.Rented.name().equals(request.status())) {
            throw new BusinessException("Không thể tạo xe ở trạng thái Rented. Trạng thái này chỉ được đặt tự động khi có đơn thuê.");
        }

        Car car = carMapper.toEntity(request);
        car.setProducer(producer);

        Car savedCar = carRepository.save(car);
        log.info("Created new car with ID: {}", savedCar.getCarId());
        return carMapper.toResponse(savedCar);
    }

    @Transactional
    @Override
    public CarResponse updateCar(Integer carId, CarRequest request) {
        Car car = getCarEntity(carId);
        CarProducer producer = carProducerRepository.findById(request.producerId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy nhà sản xuất với mã: " + request.producerId()));

        // Business Rule: car status must stay consistent with its rentals.
        boolean hasActiveRental = carRentalRepository.existsByCar_CarIdAndStatus(carId, RentalStatus.Active);
        boolean requestRented = CarStatus.Rented.name().equals(request.status());
        if (hasActiveRental && !requestRented) {
            throw new BusinessException("Xe đang có đơn thuê Active. Hãy hoàn thành hoặc hủy đơn trước khi đổi trạng thái xe.");
        }
        if (!hasActiveRental && requestRented) {
            throw new BusinessException("Xe không có đơn thuê Active nên không thể đặt trạng thái Rented.");
        }

        carMapper.updateEntityFromRequest(request, car);
        car.setProducer(producer);

        Car updatedCar = carRepository.save(car);
        log.info("Updated car with ID: {}", updatedCar.getCarId());
        return carMapper.toResponse(updatedCar);
    }

    @Transactional
    @Override
    public CarDeleteResult deleteCar(Integer carId) {
        Car car = getCarEntity(carId);
        if (carRentalRepository.existsByCar_CarIdAndStatus(carId, RentalStatus.Active)) {
            throw new BusinessException("Không thể xóa xe '" + car.getCarName() + "' vì xe đang được thuê (có đơn Active).");
        }
        boolean hasRentalHistory = carRentalRepository.existsByCar_CarId(carId);

        if (hasRentalHistory) {
            log.info("Car with ID {} has rental transactions. Soft-deleting by setting status to Inactive.", carId);
            car.setStatus(CarStatus.Inactive);
            carRepository.save(car);
            return new CarDeleteResult(false, car.getCarName(), carId);
        } else {
            log.info("Car with ID {} has no rental transactions. Physically deleting from DB.", carId);
            carRepository.delete(car);
            return new CarDeleteResult(true, car.getCarName(), carId);
        }
    }
}
