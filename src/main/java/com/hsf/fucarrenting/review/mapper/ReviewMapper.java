package com.hsf.fucarrenting.review.mapper;

import com.hsf.fucarrenting.review.entity.Review;
import com.hsf.fucarrenting.review.dto.ReviewResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReviewMapper {

    @Mapping(target = "carRentId", source = "carRental.carRentId")
    ReviewResponse toResponse(Review review);
}

