package com.hsf.fucarrenting.customer.dto;

import com.hsf.fucarrenting.common.validation.DriverDates;
import com.hsf.fucarrenting.common.validation.ValidDriverDates;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@ValidDriverDates
public record CustomerProfileRequest(
        @NotBlank(message = "Họ và tên không được để trống")
        @Size(max = 200, message = "Họ và tên không được vượt quá 200 ký tự")
        String fullName,

        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(
                regexp = "^0\\d{9}$",
                message = "Số điện thoại không hợp lệ (gồm 10 chữ số bắt đầu bằng số 0)"
        )
        String mobile,

        @NotNull(message = "Ngày sinh không được để trống")
        @Past(message = "Ngày sinh phải trong quá khứ")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate birthday,

        @NotBlank(message = "CCCD/CMND không được để trống")
        @Pattern(
                regexp = "^\\d{9,12}$",
                message = "Số CCCD/CMND phải gồm 9 đến 12 chữ số"
        )
        String identityCard,

        @NotBlank(message = "Số bằng lái không được để trống")
        @Size(max = 20, message = "Số bằng lái không được vượt quá 20 ký tự")
        String licenceNumber,

        @NotNull(message = "Ngày cấp bằng lái không được để trống")
        @PastOrPresent(message = "Ngày cấp bằng lái không được ở tương lai")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate licenceDate
) implements DriverDates {
    /**
     * Normalizes text input once, so services and mappers receive trimmed values.
     */
    public CustomerProfileRequest {
        fullName = trim(fullName);
        mobile = trim(mobile);
        identityCard = trim(identityCard);
        licenceNumber = trim(licenceNumber);
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }
}
