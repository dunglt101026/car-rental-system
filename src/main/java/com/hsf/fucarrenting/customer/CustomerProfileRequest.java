package com.hsf.fucarrenting.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record CustomerProfileRequest(
        @NotBlank(message = "Họ và tên không được để trống")
        @Size(max = 200, message = "Họ và tên không được vượt quá 200 ký tự")
        String fullName,

        @NotBlank(message = "Số điện thoại không được để trống")
        @Size(max = 15, message = "Số điện thoại không được vượt quá 15 ký tự")
        String mobile,

        @NotNull(message = "Ngày sinh không được để trống")
        @Past(message = "Ngày sinh phải trong quá khứ")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate birthday,

        @NotBlank(message = "CCCD/CMND không được để trống")
        @Size(max = 20, message = "CCCD/CMND không được vượt quá 20 ký tự")
        String identityCard,

        @NotBlank(message = "Số bằng lái không được để trống")
        @Size(max = 20, message = "Số bằng lái không được vượt quá 20 ký tự")
        String licenceNumber,

        @NotNull(message = "Ngày cấp bằng lái không được để trống")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate licenceDate
) {}
