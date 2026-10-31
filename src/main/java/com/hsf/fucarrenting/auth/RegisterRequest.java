package com.hsf.fucarrenting.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank(message = "Tên đăng nhập không được để trống")
        String accountName,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        String email,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$",
                message = "Mật khẩu tối thiểu 8 ký tự, bao gồm ít nhất 1 chữ cái và 1 chữ số"
        )
        String password,

        @NotBlank(message = "Họ và tên không được để trống")
        String fullName,

        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(
                regexp = "^0\\d{9}$",
                message = "Số điện thoại không hợp lệ (gồm 10 chữ số bắt đầu bằng số 0)"
        )
        String mobile,

        @NotNull(message = "Ngày sinh không được để trống")
        @Past(message = "Ngày sinh phải là một ngày trong quá khứ")
        LocalDate birthday,

        @NotBlank(message = "Số CCCD/CMND không được để trống")
        @Pattern(
                regexp = "^\\d{9,12}$",
                message = "Số CCCD/CMND phải gồm 9 đến 12 chữ số"
        )
        String identityCard,

        @NotBlank(message = "Số giấy phép lái xe không được để trống")
        String licenceNumber,

        @NotNull(message = "Ngày cấp bằng lái không được để trống")
        @Past(message = "Ngày cấp bằng lái phải trước ngày hiện tại")
        LocalDate licenceDate
) {
    /**
     * Normalizes text input once, so services and mappers receive trimmed values.
     * The password is kept as typed.
     */
    public RegisterRequest {
        accountName = trim(accountName);
        email = trim(email);
        fullName = trim(fullName);
        mobile = trim(mobile);
        identityCard = trim(identityCard);
        licenceNumber = trim(licenceNumber);
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }
}
