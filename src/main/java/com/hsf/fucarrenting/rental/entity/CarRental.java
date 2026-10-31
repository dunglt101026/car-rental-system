package com.hsf.fucarrenting.rental.entity;

import com.hsf.fucarrenting.car.entity.Car;
import com.hsf.fucarrenting.customer.entity.Customer;
import com.hsf.fucarrenting.review.Review;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "car_rental")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"customer", "car", "review"})
public class CarRental implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "car_rent_id")
    private Integer carRentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @Column(name = "pickup_date", nullable = false)
    private LocalDate pickupDate;

    @Column(name = "return_date", nullable = false)
    private LocalDate returnDate;

    @Column(name = "rent_price", precision = 10, scale = 0, nullable = false)
    private BigDecimal rentPrice;

    @Column(name = "status", length = 10, nullable = false)
    @Convert(converter = RentalStatusConverter.class)
    private RentalStatus status;

    @OneToOne(mappedBy = "carRental", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Review review;
}

