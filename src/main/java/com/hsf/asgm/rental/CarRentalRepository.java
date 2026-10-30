package com.hsf.asgm.rental;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarRentalRepository extends JpaRepository<CarRental, Integer>, JpaSpecificationExecutor<CarRental> {

    @Query("SELECT r FROM CarRental r " +
            "JOIN FETCH r.customer c JOIN FETCH c.account " +
            "JOIN FETCH r.car car JOIN FETCH car.producer " +
            "LEFT JOIN FETCH r.review " +
            "WHERE c.customerId = :customerId ORDER BY r.carRentId DESC")
    List<CarRental> findByCustomerIdWithDetails(@Param("customerId") Integer customerId);

    @EntityGraph(attributePaths = {"customer.account", "car.producer", "review"})
    Optional<CarRental> findWithDetailsByCarRentId(Integer carRentId);

    boolean existsByCar_CarId(Integer carId);
    boolean existsByCustomer_CustomerId(Integer customerId);
}
