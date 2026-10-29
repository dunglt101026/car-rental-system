package com.hsf.asgm.car;

import com.hsf.asgm.common.exception.BusinessException;
import com.hsf.asgm.common.exception.ResourceNotFoundException;
import com.hsf.asgm.rental.CarRentalRepository;
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
public class CarService {

    private final CarRepository carRepository;
    private final CarProducerRepository carProducerRepository;
    private final CarRentalRepository carRentalRepository;
    private final CarMapper carMapper;

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
                .map(carMapper::toResponse)
                .toList();
    }

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
        return carRepository.findAll(spec, pageable).map(carMapper::toResponse);
    }

    public CarResponse getCarById(Integer carId) {
        Car car = getCarEntity(carId);
        return carMapper.toResponse(car);
    }

    public Car getCarEntity(Integer carId) {
        return carRepository.findById(carId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin xe với mã: " + carId));
    }

    public CarRequest getCarFormById(Integer carId) {
        Car car = getCarEntity(carId);
        return CarRequest.fromEntity(car);
    }

    public List<CarProducerResponse> getAllProducers() {
        return carProducerRepository.findAllByOrderByProducerNameAsc()
                .stream()
                .map(carMapper::toProducerResponse)
                .toList();
    }

    @Transactional
    public CarResponse createCar(CarRequest request) {
        CarProducer producer = carProducerRepository.findById(request.producerId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy nhà sản xuất với mã: " + request.producerId()));

        Car car = Car.builder()
                .carName(request.carName().trim())
                .carModelYear(request.carModelYear())
                .color(request.color().trim())
                .capacity(request.capacity())
                .description(request.description().trim())
                .importDate(request.importDate())
                .producer(producer)
                .rentPrice(request.rentPrice())
                .status(request.status())
                .build();

        Car savedCar = carRepository.save(car);
        log.info("Created new car with ID: {}", savedCar.getCarId());
        return carMapper.toResponse(savedCar);
    }

    @Transactional
    public CarResponse updateCar(Integer carId, CarRequest request) {
        Car car = getCarEntity(carId);
        CarProducer producer = carProducerRepository.findById(request.producerId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy nhà sản xuất với mã: " + request.producerId()));

        car.setCarName(request.carName().trim());
        car.setCarModelYear(request.carModelYear());
        car.setColor(request.color().trim());
        car.setCapacity(request.capacity());
        car.setDescription(request.description().trim());
        car.setImportDate(request.importDate());
        car.setProducer(producer);
        car.setRentPrice(request.rentPrice());
        car.setStatus(request.status());

        Car updatedCar = carRepository.save(car);
        log.info("Updated car with ID: {}", updatedCar.getCarId());
        return carMapper.toResponse(updatedCar);
    }

    @Transactional
    public CarDeleteResult deleteCar(Integer carId) {
        Car car = getCarEntity(carId);
        boolean hasRentalHistory = carRentalRepository.existsByCar_CarId(carId);

        if (hasRentalHistory) {
            log.info("Car with ID {} has rental transactions. Soft-deleting by setting status to Inactive.", carId);
            car.setStatus(CarStatus.Inactive.name());
            carRepository.save(car);
            return new CarDeleteResult(false, car.getCarName(), carId);
        } else {
            log.info("Car with ID {} has no rental transactions. Physically deleting from DB.", carId);
            carRepository.delete(car);
            return new CarDeleteResult(true, car.getCarName(), carId);
        }
    }

    @Transactional
    public void updateCarStatus(Integer carId, String status) {
        Car car = getCarEntity(carId);
        car.setStatus(status);
        carRepository.save(car);
    }
}
