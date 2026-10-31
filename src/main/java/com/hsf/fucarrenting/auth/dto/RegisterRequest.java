package com.hsf.fucarrenting.auth.dto;

import com.hsf.fucarrenting.common.validation.DriverDates;
import com.hsf.fucarrenting.common.validation.ValidDriverDates;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@ValidDriverDates
public record RegisterRequest(
        @NotBlank(message = "Tên đăng nhập không được để trống")
        @Size(max = 100, message = "Tên đăng nhập không được vượt quá 100 ký tự")
        String accountName,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        @Size(max = 200, message = "Email không được vượt quá 200 ký tự")
        String email,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$",
                message = "Mật khẩu tối thiểu 8 ký tự, bao gồm ít nhất 1 chữ cái và 1 chữ số"
        )
        String password,

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
        @Past(message = "Ngày sinh phải là một ngày trong quá khứ")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate birthday,

        @NotBlank(message = "Số CCCD/CMND không được để trống")
        @Pattern(
                regexp = "^\\d{9,12}$",
                message = "Số CCCD/CMND phải gồm 9 đến 12 chữ số"
        )
        String identityCard,

        @NotBlank(message = "Số giấy phép lái xe không được để trống")
        @Size(max = 20, message = "Số giấy phép lái xe không được vượt quá 20 ký tự")
        String licenceNumber,

        @NotNull(message = "Ngày cấp bằng lái không được để trống")
        @PastOrPresent(message = "Ngày cấp bằng lái không được ở tương lai")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate licenceDate
) implements DriverDates {
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
