package com.hsf.asgm.rental;

import com.hsf.asgm.car.Car;
import com.hsf.asgm.car.CarRepository;
import com.hsf.asgm.car.CarStatus;
import com.hsf.asgm.common.exception.BusinessException;
import com.hsf.asgm.common.exception.ResourceNotFoundException;
import com.hsf.asgm.customer.Customer;
import com.hsf.asgm.customer.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RentalService {

    private final CarRentalRepository carRentalRepository;
    private final CarRepository carRepository;
    private final CustomerRepository customerRepository;
    private final RentalMapper rentalMapper;

    public List<RentalResponse> getCustomerRentals(Integer customerId) {
        return carRentalRepository.findByCustomerIdWithDetails(customerId)
                .stream()
                .map(rentalMapper::toResponse)
                .toList();
    }

    public RentalResponse getRentalById(Integer carRentId) {
        CarRental rental = getRentalEntity(carRentId);
        return rentalMapper.toResponse(rental);
    }

    public CarRental getRentalEntity(Integer carRentId) {
        return carRentalRepository.findById(carRentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn thuê xe với mã: " + carRentId));
    }

    @Transactional
    public RentalResponse bookCar(Integer customerId, RentalCreateRequest request) {
        if (request.pickupDate() == null || request.returnDate() == null) {
            throw new BusinessException("Vui lòng chọn ngày nhận xe và ngày trả xe.");
        }

        if (request.pickupDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Ngày nhận xe không thể ở trong quá khứ.");
        }

        if (!request.returnDate().isAfter(request.pickupDate())) {
            throw new BusinessException("Ngày trả xe phải sau ngày nhận xe ít nhất 1 ngày.");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin khách hàng với mã: " + customerId));

        Car car = carRepository.findById(request.carId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy xe với mã: " + request.carId()));

        if (!CarStatus.Available.name().equalsIgnoreCase(car.getStatus())) {
            throw new BusinessException("Xe '" + car.getCarName() + "' hiện không khả dụng để đặt thuê (trạng thái: " + car.getStatus() + ").");
        }

        long days = ChronoUnit.DAYS.between(request.pickupDate(), request.returnDate());
        if (days < 1) {
            days = 1;
        }

        BigDecimal totalRentPrice = car.getRentPrice().multiply(BigDecimal.valueOf(days));

        CarRental rental = CarRental.builder()
                .customer(customer)
                .car(car)
                .pickupDate(request.pickupDate())
                .returnDate(request.returnDate())
                .rentPrice(totalRentPrice)
                .status(RentalStatus.Active.name())
                .build();

        CarRental savedRental = carRentalRepository.save(rental);

        // Update car status to Rented
        car.setStatus(CarStatus.Rented.name());
        carRepository.save(car);

        log.info("Car booked successfully: rentalId={}, customerId={}, carId={}, totalPrice={}",
                savedRental.getCarRentId(), customerId, car.getCarId(), totalRentPrice);

        return rentalMapper.toResponse(savedRental);
    }
}
