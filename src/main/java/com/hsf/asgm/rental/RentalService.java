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
public class RentalService {

    private final CarRentalRepository carRentalRepository;
    private final CarRepository carRepository;
    private final CustomerRepository customerRepository;
    private final RentalMapper rentalMapper;

    public Page<RentalResponse> getAdminRentals(String keyword, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "carRentId"));
        Specification<CarRental> spec = RentalSpecification.filterRentals(keyword, status);
        return carRentalRepository.findAll(spec, pageable).map(rentalMapper::toResponse);
    }

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

    /**
     * Books one or many cars for the same period. Each car becomes its own car_rental record.
     * All cars must be Available, otherwise nothing is booked.
     */
    @Transactional
    public List<RentalResponse> bookCars(Integer customerId, RentalCreateRequest request) {
        if (request.carIds() == null || request.carIds().isEmpty()) {
            throw new BusinessException("Vui lòng chọn ít nhất một xe để đặt thuê.");
        }
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

        // Validate every car before saving anything
        List<Car> cars = request.carIds().stream()
                .distinct()
                .map(carId -> carRepository.findById(carId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy xe với mã: " + carId)))
                .toList();

        for (Car car : cars) {
            if (!CarStatus.Available.name().equalsIgnoreCase(car.getStatus())) {
                throw new BusinessException("Xe '" + car.getCarName() + "' hiện không khả dụng để đặt thuê (trạng thái: " + car.getStatus() + ").");
            }
        }

        long days = ChronoUnit.DAYS.between(request.pickupDate(), request.returnDate());

        return cars.stream().map(car -> {
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
        }).toList();
    }

    @Transactional
    public RentalResponse updateRentalStatus(Integer carRentId, String newStatus) {
        CarRental rental = getRentalEntity(carRentId);
        String oldStatus = rental.getStatus();

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
        if (!RentalStatus.Active.name().equalsIgnoreCase(oldStatus)) {
            throw new BusinessException("Đơn thuê #" + carRentId + " đã kết thúc (" + oldStatus
                    + "), không thể thay đổi trạng thái.");
        }
        if (targetStatus == RentalStatus.Active) {
            throw new BusinessException("Đơn thuê #" + carRentId + " đang ở trạng thái Active.");
        }

        // Release the car back to Available status
        Car car = rental.getCar();
        if (car != null && CarStatus.Rented.name().equalsIgnoreCase(car.getStatus())) {
            car.setStatus(CarStatus.Available.name());
            carRepository.save(car);
            log.info("Car ID {} status updated to Available because rental ID {} changed from {} to {}",
                    car.getCarId(), carRentId, oldStatus, targetStatus.name());
        }

        rental.setStatus(targetStatus.name());
        CarRental saved = carRentalRepository.save(rental);
        log.info("Updated rental ID {} status from {} to {}", carRentId, oldStatus, targetStatus.name());
        return rentalMapper.toResponse(saved);
    }

    public RentalReportResponse generateReport(LocalDate startDate, LocalDate endDate, String sortBy) {
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate.isAfter(endDate)) {
            LocalDate temp = startDate;
            startDate = endDate;
            endDate = temp;
        }

        String validSortBy = "revenue".equalsIgnoreCase(sortBy) ? "revenue" : "date";

        Sort sort = "revenue".equalsIgnoreCase(validSortBy)
                ? Sort.by(Sort.Direction.DESC, "rentPrice").and(Sort.by(Sort.Direction.DESC, "carRentId"))
                : Sort.by(Sort.Direction.DESC, "pickupDate").and(Sort.by(Sort.Direction.DESC, "carRentId"));

        Specification<CarRental> spec = RentalSpecification.filterReport(startDate, endDate);
        List<CarRental> rentals = carRentalRepository.findAll(spec, sort);

        List<RentalResponse> rentalResponses = rentals.stream()
                .map(rentalMapper::toResponse)
                .toList();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal completedRevenue = BigDecimal.ZERO;
        long activeCount = 0;
        long completedCount = 0;
        long cancelledCount = 0;

        for (CarRental r : rentals) {
            // Cancelled rentals generate no revenue
            if (r.getRentPrice() != null && !RentalStatus.Cancelled.name().equalsIgnoreCase(r.getStatus())) {
                totalRevenue = totalRevenue.add(r.getRentPrice());
                if (RentalStatus.Completed.name().equalsIgnoreCase(r.getStatus())) {
                    completedRevenue = completedRevenue.add(r.getRentPrice());
                }
            }
            if (RentalStatus.Active.name().equalsIgnoreCase(r.getStatus())) {
                activeCount++;
            } else if (RentalStatus.Completed.name().equalsIgnoreCase(r.getStatus())) {
                completedCount++;
            } else if (RentalStatus.Cancelled.name().equalsIgnoreCase(r.getStatus())) {
                cancelledCount++;
            }
        }

        return new RentalReportResponse(
                startDate,
                endDate,
                validSortBy,
                rentalResponses.size(),
                totalRevenue,
                completedRevenue,
                activeCount,
                completedCount,
                cancelledCount,
                rentalResponses
        );
    }
}
