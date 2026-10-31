package com.hsf.fucarrenting.review;

public record ReviewResponse(
        Integer id,
        Integer carRentId,
        Integer reviewStar,
        String comment
) {}

