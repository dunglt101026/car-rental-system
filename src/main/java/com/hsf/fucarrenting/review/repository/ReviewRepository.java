package com.hsf.fucarrenting.review.repository;

import com.hsf.fucarrenting.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    Optional<Review> findByCarRental_CarRentId(Integer carRentId);
    boolean existsByCarRental_CarRentId(Integer carRentId);
}


