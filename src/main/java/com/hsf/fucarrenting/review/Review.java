package com.hsf.fucarrenting.review;

import com.hsf.fucarrenting.rental.CarRental;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "carRental")
public class Review implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_rent_id", nullable = false)
    private CarRental carRental;

    @Column(name = "review_star", nullable = false)
    private Integer reviewStar;

    @Column(name = "comment", length = 500, nullable = false)
    private String comment;
}

