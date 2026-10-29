package com.hsf.asgm.rental;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RentalMapper {

    @Mapping(target = "customerId", source = "customer.customerId")
    @Mapping(target = "customerFullName", source = "customer.fullName")
    @Mapping(target = "customerMobile", source = "customer.mobile")
    @Mapping(target = "customerEmail", source = "customer.account.email")
    @Mapping(target = "carId", source = "car.carId")
    @Mapping(target = "carName", source = "car.carName")
    @Mapping(target = "carProducerName", source = "car.producer.producerName")
    @Mapping(target = "reviewStar", source = "review.reviewStar")
    @Mapping(target = "reviewComment", source = "review.comment")
    RentalResponse toResponse(CarRental carRental);
}
