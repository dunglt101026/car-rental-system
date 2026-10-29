package com.hsf.asgm.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record CustomerProfileRequest(
        @NotBlank(message = "Họ và tên không được để trống")
        String fullName,

        @NotBlank(message = "Số điện thoại không được để trống")
        String mobile,

        @NotNull(message = "Ngày sinh không được để trống")
        @Past(message = "Ngày sinh phải trong quá khứ")
        LocalDate birthday,

        @NotBlank(message = "CCCD/CMND không được để trống")
        String identityCard,

        @NotBlank(message = "Số bằng lái không được để trống")
        String licenceNumber,

        @NotNull(message = "Ngày cấp bằng lái không được để trống")
        LocalDate licenceDate
) {}

