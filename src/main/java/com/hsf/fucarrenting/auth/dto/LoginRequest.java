package com.hsf.fucarrenting.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Tên đăng nhập không được để trống")
        String accountName,

        @NotBlank(message = "Mật khẩu không được để trống")
        String password
) {}

