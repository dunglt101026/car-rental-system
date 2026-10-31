package com.hsf.fucarrenting.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewCreateRequest(
        @NotNull(message = "Mã đơn thuê không được để trống")
        Integer carRentId,

        @NotNull(message = "Số sao đánh giá không được để trống")
        @Min(value = 1, message = "Số sao tối thiểu là 1")
        @Max(value = 5, message = "Số sao tối đa là 5")
        Integer reviewStar,

        @NotBlank(message = "Nội dung nhận xét không được để trống")
        String comment
) {}

