package com.hsf.fucarrenting.car;

import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface CarService {
    List<CarResponse> getAvailableCars(
            Integer producerId,
            Integer capacity,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String keyword
    );

    Page<CarResponse> getAdminCars(
            String keyword,
            Integer producerId,
            String status,
            int page,
            int size
    );

    CarResponse getCarById(Integer carId);

    Car getCarEntity(Integer carId);

    CarRequest getCarFormById(Integer carId);

    List<CarProducerResponse> getAllProducers();

    @Transactional
    CarResponse createCar(CarRequest request);

    @Transactional
    CarResponse updateCar(Integer carId, CarRequest request);

    @Transactional
    CarDeleteResult deleteCar(Integer carId);
}
