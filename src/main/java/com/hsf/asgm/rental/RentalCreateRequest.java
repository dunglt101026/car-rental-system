package com.hsf.asgm.rental;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RentalCreateRequest(
        @NotNull(message = "Vui lòng chọn xe")
        Integer carId,

        @NotNull(message = "Ngày nhận xe không được để trống")
        @FutureOrPresent(message = "Ngày nhận xe không thể ở quá khứ")
        LocalDate pickupDate,

        @NotNull(message = "Ngày trả xe không được để trống")
        LocalDate returnDate
) {}

