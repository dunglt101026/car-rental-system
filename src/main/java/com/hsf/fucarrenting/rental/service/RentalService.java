package com.hsf.fucarrenting.rental.service;

import com.hsf.fucarrenting.rental.entity.CarRental;
import com.hsf.fucarrenting.rental.dto.RentalCreateRequest;
import com.hsf.fucarrenting.rental.dto.RentalQuoteResponse;
import com.hsf.fucarrenting.rental.dto.RentalResponse;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RentalService {
    Page<RentalResponse> getAdminRentals(String keyword, String status, int page, int size);

    List<RentalResponse> getCustomerRentals(Integer customerId);

    RentalResponse getRentalById(Integer carRentId);

    CarRental getRentalEntity(Integer carRentId);

    RentalQuoteResponse quoteRental(RentalCreateRequest request);

    @Transactional
    List<RentalResponse> bookCars(Integer customerId, RentalCreateRequest request);

    @Transactional
    RentalResponse updateRentalStatus(Integer carRentId, String newStatus);
}
