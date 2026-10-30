package com.hsf.asgm.car;

import com.hsf.asgm.rental.CarRental;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "car")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"producer", "carRentals"})
public class Car implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "car_id")
    private Integer carId;

    @Column(name = "car_name", length = 200, nullable = false)
    private String carName;

    @Column(name = "car_model_year", nullable = false)
    private Integer carModelYear;

    @Column(name = "color", length = 50, nullable = false)
    private String color;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "description", length = 1000, nullable = false)
    private String description;

    @Column(name = "import_date", nullable = false)
    private LocalDate importDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producer_id", nullable = false)
    private CarProducer producer;

    @Column(name = "rent_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal rentPrice;

    @Column(name = "status", length = 10, nullable = false)
    @Enumerated(EnumType.STRING)
    private CarStatus status;

    @OneToMany(mappedBy = "car", fetch = FetchType.LAZY)
    @Builder.Default
    private List<CarRental> carRentals = new ArrayList<>();
}

