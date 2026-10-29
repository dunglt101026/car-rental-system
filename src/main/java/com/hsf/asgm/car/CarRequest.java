package com.hsf.asgm.car;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CarRequest(
        @NotBlank(message = "Tên xe không được để trống")
        @Size(max = 200, message = "Tên xe không được vượt quá 200 ký tự")
        String carName,

        @NotNull(message = "Năm sản xuất không được để trống")
        @Min(value = 1900, message = "Năm sản xuất tối thiểu là 1900")
        @Max(value = 2100, message = "Năm sản xuất không hợp lệ")
        Integer carModelYear,

        @NotBlank(message = "Màu xe không được để trống")
        @Size(max = 50, message = "Màu xe không được vượt quá 50 ký tự")
        String color,

        @NotNull(message = "Số chỗ ngồi không được để trống")
        @Min(value = 1, message = "Số chỗ ngồi phải lớn hơn 0")
        @Max(value = 100, message = "Số chỗ ngồi tối đa 100")
        Integer capacity,

        @NotBlank(message = "Mô tả không được để trống")
        @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
        String description,

        @NotNull(message = "Ngày nhập không được để trống")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate importDate,

        @NotNull(message = "Vui lòng chọn nhà sản xuất")
        Integer producerId,

        @NotNull(message = "Giá thuê không được để trống")
        @DecimalMin(value = "0.01", message = "Giá thuê phải lớn hơn 0")
        BigDecimal rentPrice,

        @NotBlank(message = "Trạng thái xe không được để trống")
        String status
) {
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

    public static CarRequest fromEntity(Car car) {
        return new CarRequest(
                car.getCarName(),
                car.getCarModelYear(),
                car.getColor(),
                car.getCapacity(),
                car.getDescription(),
                car.getImportDate(),
                car.getProducer() != null ? car.getProducer().getProducerId() : null,
                car.getRentPrice(),
                car.getStatus()
        );
    }
}
