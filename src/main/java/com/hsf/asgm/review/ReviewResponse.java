package com.hsf.asgm.review;

public record ReviewResponse(
        Integer id,
        Integer carRentId,
        Integer reviewStar,
        String comment
) {}

