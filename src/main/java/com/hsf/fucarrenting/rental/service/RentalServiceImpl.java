package com.hsf.fucarrenting.rental.service;

import com.hsf.fucarrenting.car.entity.Car;
import com.hsf.fucarrenting.car.repository.CarRepository;
import com.hsf.fucarrenting.car.entity.CarStatus;
import com.hsf.fucarrenting.common.exception.BusinessException;
import com.hsf.fucarrenting.common.exception.ResourceNotFoundException;
import com.hsf.fucarrenting.customer.entity.Customer;
import com.hsf.fucarrenting.customer.repository.CustomerRepository;
import com.hsf.fucarrenting.rental.entity.CarRental;
import com.hsf.fucarrenting.rental.repository.CarRentalRepository;
import com.hsf.fucarrenting.rental.repository.RentalSpecification;
import com.hsf.fucarrenting.rental.entity.RentalStatus;
import com.hsf.fucarrenting.rental.dto.RentalCreateRequest;
import com.hsf.fucarrenting.rental.dto.RentalQuoteResponse;
import com.hsf.fucarrenting.rental.dto.RentalResponse;
import com.hsf.fucarrenting.rental.mapper.RentalMapper;
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
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RentalServiceImpl implements RentalService {

    private final CarRentalRepository carRentalRepository;
    private final CarRepository carRepository;
    private final CustomerRepository customerRepository;
    private final RentalMapper rentalMapper;

    @Override
    public Page<RentalResponse> getAdminRentals(String keyword, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "carRentId"));
        Specification<CarRental> spec = RentalSpecification.filterRentals(keyword, status);
        return carRentalRepository.findAll(spec, pageable).map(rental -> rentalMapper.toResponse(rental));
    }

    @Override
    public List<RentalResponse> getCustomerRentals(Integer customerId) {
        return carRentalRepository.findByCustomerIdWithDetails(customerId)
                .stream()
                .map(rental -> rentalMapper.toResponse(rental))
                .toList();
    }

    @Override
    public RentalResponse getRentalById(Integer carRentId) {
        CarRental rental = getRentalEntity(carRentId);
        return rentalMapper.toResponse(rental);
    }

    @Override
    public CarRental getRentalEntity(Integer carRentId) {
        return carRentalRepository.findWithDetailsByCarRentId(carRentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn thuê xe với mã: " + carRentId));
    }

    /**
     * Estimates the cost of a booking without saving anything. Runs the same checks as {@link #bookCars}.
     */
    @Override
    public RentalQuoteResponse quoteRental(RentalCreateRequest request) {
        long days = validateBookingDates(request);
        List<RentalQuoteResponse.Line> lines = loadAvailableCars(request.carIds()).stream()
                .map(car -> new RentalQuoteResponse.Line(
                        car.getCarId(),
                        car.getCarName(),
                        car.getRentPrice(),
                        car.getRentPrice().multiply(BigDecimal.valueOf(days))))
                .toList();
        BigDecimal total = lines.stream()
                .map(line -> line.subtotal())
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
        return new RentalQuoteResponse(days, lines, total);
    }

    /**
     * Books one or many cars for the same period. Each car becomes its own car_rental record.
     * All cars must be Available, otherwise nothing is booked.
     */
    @Transactional
    @Override
    public List<RentalResponse> bookCars(Integer customerId, RentalCreateRequest request) {
        long days = validateBookingDates(request);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin khách hàng với mã: " + customerId));

        // Validate every car before saving anything
        List<Car> cars = loadAvailableCars(request.carIds());

        return cars.stream().map(car -> {
            BigDecimal totalRentPrice = car.getRentPrice().multiply(BigDecimal.valueOf(days));

            CarRental rental = CarRental.builder()
                    .customer(customer)
                    .car(car)
                    .pickupDate(request.pickupDate())
                    .returnDate(request.returnDate())
                    .rentPrice(totalRentPrice)
                    .status(RentalStatus.Active)
                    .build();

            CarRental savedRental = carRentalRepository.save(rental);

            // Update car status to Rented
            car.setStatus(CarStatus.Rented);
            carRepository.save(car);

            log.info("Car booked successfully: rentalId={}, customerId={}, carId={}, totalPrice={}",
                    savedRental.getCarRentId(), customerId, car.getCarId(), totalRentPrice);

            return rentalMapper.toResponse(savedRental);
        }).toList();
    }

    /**
     * Checks the booking period and returns the number of rental days.
     */
    private long validateBookingDates(RentalCreateRequest request) {
        if (request.pickupDate() == null || request.returnDate() == null) {
            throw new BusinessException("Vui lòng chọn ngày nhận xe và ngày trả xe.");
        }

        if (request.pickupDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Ngày nhận xe không thể ở trong quá khứ.");
        }

        if (!request.returnDate().isAfter(request.pickupDate())) {
            throw new BusinessException("Ngày trả xe phải sau ngày nhận xe ít nhất 1 ngày.");
        }

        return ChronoUnit.DAYS.between(request.pickupDate(), request.returnDate());
    }

    /**
     * Loads the selected cars and makes sure every one of them is Available.
     */
    private List<Car> loadAvailableCars(List<Integer> carIds) {
        if (carIds == null || carIds.isEmpty()) {
            throw new BusinessException("Vui lòng chọn ít nhất một xe để đặt thuê.");
        }

        List<Car> cars = carIds.stream()
                .distinct()
                .map(carId -> carRepository.findById(carId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy xe với mã: " + carId)))
                .toList();

        for (Car car : cars) {
            if (car.getStatus() != CarStatus.Available) {
                throw new BusinessException("Xe '" + car.getCarName() + "' hiện không khả dụng để đặt thuê (trạng thái: " + car.getStatus() + ").");
            }
        }
        return cars;
    }

    @Transactional
    @Override
    public RentalResponse updateRentalStatus(Integer carRentId, String newStatus) {
        CarRental rental = getRentalEntity(carRentId);
        RentalStatus oldStatus = rental.getStatus();

        if (newStatus == null || newStatus.isBlank()) {
            throw new BusinessException("Trạng thái đơn thuê không hợp lệ: " + newStatus);
        }
        RentalStatus targetStatus;
        try {
            targetStatus = RentalStatus.valueOf(newStatus.trim());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Trạng thái đơn thuê không hợp lệ: " + newStatus);
        }

        // Business Rule: Completed and Cancelled are final states.
        // Only an Active rental can move, and only to Completed or Cancelled.
        if (oldStatus != RentalStatus.Active) {
            throw new BusinessException("Đơn thuê #" + carRentId + " đã kết thúc (" + oldStatus
                    + "), không thể thay đổi trạng thái.");
        }
        if (targetStatus == RentalStatus.Active) {
            throw new BusinessException("Đơn thuê #" + carRentId + " đang ở trạng thái Active.");
        }
        // A car cannot be returned before it has been picked up; such a booking can only be cancelled
        if (targetStatus == RentalStatus.Completed && rental.getPickupDate().isAfter(LocalDate.now())) {
            throw new BusinessException("Đơn thuê #" + carRentId + " chưa đến ngày nhận xe nên chưa thể xác nhận trả xe."
                    + " Nếu khách không thuê nữa, hãy hủy đơn.");
        }

        // Release the car back to Available status
        Car car = rental.getCar();
        if (car != null && car.getStatus() == CarStatus.Rented) {
            car.setStatus(CarStatus.Available);
            carRepository.save(car);
            log.info("Car ID {} status updated to Available because rental ID {} changed from {} to {}",
                    car.getCarId(), carRentId, oldStatus, targetStatus.name());
        }

        rental.setStatus(targetStatus);
        CarRental saved = carRentalRepository.save(rental);
        log.info("Updated rental ID {} status from {} to {}", carRentId, oldStatus, targetStatus.name());
        return rentalMapper.toResponse(saved);
    }
}
