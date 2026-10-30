package com.hsf.asgm.rental;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record RentalCreateRequest(
        @NotEmpty(message = "Vui lòng chọn ít nhất một xe")
        List<Integer> carIds,

        @NotNull(message = "Ngày nhận xe không được để trống")
        @FutureOrPresent(message = "Ngày nhận xe không thể ở quá khứ")
        LocalDate pickupDate,

        @NotNull(message = "Ngày trả xe không được để trống")
        LocalDate returnDate
) {}
