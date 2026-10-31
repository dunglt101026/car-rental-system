package com.hsf.fucarrenting.car.dto;

import com.hsf.fucarrenting.car.entity.CarStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CarRequest(
        @NotBlank(message = "Tên xe không được để trống")
        @Size(max = 200, message = "Tên xe không được vượt quá 200 ký tự")
        String carName,

        @NotNull(message = "Năm sản xuất không được để trống")
        @Min(value = 1990, message = "Năm sản xuất tối thiểu là 1990")
        Integer carModelYear,

        @NotBlank(message = "Màu xe không được để trống")
        @Size(max = 50, message = "Màu xe không được vượt quá 50 ký tự")
        String color,

        @NotNull(message = "Số chỗ ngồi không được để trống")
        @Min(value = 2, message = "Số chỗ ngồi tối thiểu là 2")
        @Max(value = 45, message = "Số chỗ ngồi tối đa là 45")
        Integer capacity,

        @NotBlank(message = "Mô tả không được để trống")
        @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
        String description,

        @NotNull(message = "Ngày nhập không được để trống")
        @PastOrPresent(message = "Ngày nhập kho không được ở tương lai")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate importDate,

        @NotNull(message = "Vui lòng chọn nhà sản xuất")
        Integer producerId,

        @NotNull(message = "Giá thuê không được để trống")
        @DecimalMin(value = "1", message = "Giá thuê phải lớn hơn 0")
        @DecimalMax(value = "10000", message = "Giá thuê tối đa là 10000 $ / ngày")
        @Digits(integer = 10, fraction = 0, message = "Giá thuê phải là số nguyên (cột rent_price trong DB không có phần thập phân)")
        BigDecimal rentPrice,

        @NotBlank(message = "Trạng thái xe không được để trống")
        @Pattern(regexp = "Available|Rented|Inactive", message = "Trạng thái xe không hợp lệ")
        String status
) {
    /**
     * A model can be released one year early, never later. Shown under the model year input.
     */
    @AssertTrue(message = "Năm sản xuất không được vượt quá năm sau")
    public boolean isModelYearNotInFuture() {
        return carModelYear == null || carModelYear <= LocalDate.now().getYear() + 1;
    }

    /**
     * A car cannot be imported before its model year started (one year early is allowed). Shown under the import date.
     */
    @AssertTrue(message = "Ngày nhập kho không được trước năm sản xuất của xe")
    public boolean isImportDateAfterModelYear() {
        return importDate == null || carModelYear == null || importDate.getYear() >= carModelYear - 1;
    }

    public static CarRequest empty() {
        return new CarRequest(
                "",
                LocalDate.now().getYear(),
                "",
                4,
                "",
                LocalDate.now(),
                null,
                null,
                CarStatus.Available.name()
        );
    }
}
