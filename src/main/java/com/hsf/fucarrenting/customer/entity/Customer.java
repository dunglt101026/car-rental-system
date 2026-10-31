package com.hsf.fucarrenting.customer.entity;

import com.hsf.fucarrenting.auth.entity.Account;
import com.hsf.fucarrenting.rental.entity.CarRental;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"account", "carRentals"})
public class Customer implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Integer customerId;

    @Column(name = "full_name", length = 200, nullable = false)
    private String fullName;

    @Column(name = "mobile", length = 15, nullable = false)
    private String mobile;

    @Column(name = "birthday", nullable = false)
    private LocalDate birthday;

    @Column(name = "identity_card", length = 20, nullable = false)
    private String identityCard;

    @Column(name = "licence_number", length = 20, nullable = false)
    private String licenceNumber;

    @Column(name = "licence_date", nullable = false)
    private LocalDate licenceDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    @Builder.Default
    private List<CarRental> carRentals = new ArrayList<>();
}

