package com.hsf.fucarrenting.car.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CarRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private CarRequest car(int modelYear, LocalDate importDate, int capacity) {
        return new CarRequest("Toyota Vios", modelYear, "White", capacity, "Sedan", importDate, 1,
                BigDecimal.valueOf(40), "Available");
    }

    private List<String> invalidFields(CarRequest request) {
        return validator.validate(request).stream().map(v -> v.getPropertyPath().toString()).toList();
    }

    @Test
    @DisplayName("Xe hợp lệ -> Không có lỗi")
    void testValidCar_ShouldPass() {
        assertThat(invalidFields(car(2023, LocalDate.of(2023, 3, 1), 5))).isEmpty();
    }

    @Test
    @DisplayName("Năm sản xuất 2100 -> Lỗi modelYearNotInFuture")
    void testModelYearFarInFuture_ShouldReject() {
        assertThat(invalidFields(car(2100, LocalDate.now(), 5))).contains("modelYearNotInFuture");
    }

    @Test
    @DisplayName("Ngày nhập kho ở tương lai -> Lỗi importDate")
    void testImportDateInFuture_ShouldReject() {
        assertThat(invalidFields(car(LocalDate.now().getYear(), LocalDate.now().plusDays(1), 5))).contains("importDate");
    }

    @Test
    @DisplayName("Nhập kho trước năm sản xuất -> Lỗi importDateAfterModelYear")
    void testImportBeforeModelYear_ShouldReject() {
        assertThat(invalidFields(car(2024, LocalDate.of(2021, 5, 1), 5))).containsExactly("importDateAfterModelYear");
    }

    @Test
    @DisplayName("Số chỗ 100 -> Lỗi capacity")
    void testCapacityTooLarge_ShouldReject() {
        assertThat(invalidFields(car(2023, LocalDate.of(2023, 3, 1), 100))).containsExactly("capacity");
    }
}
