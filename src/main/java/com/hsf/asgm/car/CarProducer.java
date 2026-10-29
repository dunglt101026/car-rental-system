package com.hsf.asgm.car;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "car_producer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "cars")
public class CarProducer implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "producer_id")
    private Integer producerId;

    @Column(name = "producer_name", length = 100, nullable = false)
    private String producerName;

    @Column(name = "address", length = 200, nullable = false)
    private String address;

    @Column(name = "country", length = 100, nullable = false)
    private String country;

    @OneToMany(mappedBy = "producer", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Car> cars = new ArrayList<>();
}

