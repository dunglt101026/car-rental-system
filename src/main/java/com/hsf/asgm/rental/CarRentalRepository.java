package com.hsf.asgm.rental;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CarRentalRepository extends JpaRepository<CarRental, Integer>, JpaSpecificationExecutor<CarRental> {
    List<CarRental> findByCustomer_CustomerIdOrderByPickupDateDesc(Integer customerId);
    List<CarRental> findByCustomer_CustomerIdOrderByCarRentIdDesc(Integer customerId);

    @Query("SELECT r FROM CarRental r LEFT JOIN FETCH r.car LEFT JOIN FETCH r.review WHERE r.customer.customerId = :customerId ORDER BY r.carRentId DESC")
    List<CarRental> findByCustomerIdWithDetails(@Param("customerId") Integer customerId);

    List<CarRental> findByPickupDateBetweenOrderByPickupDateDesc(LocalDate startDate, LocalDate endDate);
    boolean existsByCar_CarId(Integer carId);
}
