package com.hsf.fucarrenting.review.dto;

public record ReviewResponse(
        Integer id,
        Integer carRentId,
        Integer reviewStar,
        String comment
) {}

