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
    public static final int MAX_DAYS_AHEAD = 90;
    public static final int MAX_RENTAL_DAYS = 30;

    /**
     * Shown under the return date input as field "returnDateAfterPickupDate".
     */
    @AssertTrue(message = "Ngày trả xe phải sau ngày nhận xe ít nhất 1 ngày")
    public boolean isReturnDateAfterPickupDate() {
        return pickupDate == null || returnDate == null || returnDate.isAfter(pickupDate);
    }

    /**
     * Bookings open at most 90 days ahead. Shown under the pickup date input.
     */
    @AssertTrue(message = "Chỉ nhận đặt xe trước tối đa " + MAX_DAYS_AHEAD + " ngày")
    public boolean isPickupDateWithinBookingWindow() {
        return pickupDate == null || !pickupDate.isAfter(LocalDate.now().plusDays(MAX_DAYS_AHEAD));
    }

    /**
     * One booking lasts at most 30 days. Shown under the return date input.
     */
    @AssertTrue(message = "Mỗi đơn thuê tối đa " + MAX_RENTAL_DAYS + " ngày")
    public boolean isRentalWithinMaxDays() {
        return pickupDate == null || returnDate == null || !returnDate.isAfter(pickupDate.plusDays(MAX_RENTAL_DAYS));
    }
}
