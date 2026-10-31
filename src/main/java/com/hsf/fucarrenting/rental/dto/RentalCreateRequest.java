package com.hsf.fucarrenting.rental.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

public record RentalCreateRequest(
        @NotEmpty(message = "Vui lòng chọn ít nhất một xe")
        List<Integer> carIds,

        @NotNull(message = "Ngày nhận xe không được để trống")
        @FutureOrPresent(message = "Ngày nhận xe không thể ở quá khứ")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate pickupDate,

        @NotNull(message = "Ngày trả xe không được để trống")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate returnDate
) {
    /**
     * Shown under the return date input as field "returnDateAfterPickupDate".
     */
    @AssertTrue(message = "Ngày trả xe phải sau ngày nhận xe ít nhất 1 ngày")
    public boolean isReturnDateAfterPickupDate() {
        return pickupDate == null || returnDate == null || returnDate.isAfter(pickupDate);
    }
}
