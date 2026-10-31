package com.hsf.fucarrenting.common.validation;

import com.hsf.fucarrenting.auth.dto.RegisterRequest;
import com.hsf.fucarrenting.customer.dto.CustomerProfileRequest;
import com.hsf.fucarrenting.rental.dto.RentalCreateRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DriverDatesValidatorTest {

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

    private RegisterRequest register(LocalDate birthday, LocalDate licenceDate, String licenceNumber) {
        return new RegisterRequest("newuser", "new@gmail.com", "abc12345", "Nguyễn Văn Mới",
                "0909999999", birthday, "001099999999", licenceNumber, licenceDate);
    }

    private <T> List<String> invalidFields(Set<ConstraintViolation<T>> violations) {
        return violations.stream().map(v -> v.getPropertyPath().toString()).toList();
    }

    @Test
    @DisplayName("Đăng ký hợp lệ -> Không có lỗi")
    void testValidRegister_ShouldPass() {
        RegisterRequest request = register(LocalDate.of(1995, 5, 15), LocalDate.of(2020, 1, 10), "B2-987654321");

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("Chưa đủ 18 tuổi -> Lỗi tại ô birthday")
    void testUnderAge_ShouldRejectBirthday() {
        LocalDate birthday = LocalDate.now().minusYears(17);
        RegisterRequest request = register(birthday, LocalDate.now().minusDays(1), "B2-1");

        assertThat(invalidFields(validator.validate(request))).contains("birthday");
    }

    @Test
    @DisplayName("Ngày cấp GPLX trước ngày sinh -> Lỗi tại ô licenceDate")
    void testLicenceBeforeBirthday_ShouldRejectLicenceDate() {
        RegisterRequest request = register(LocalDate.of(1995, 5, 15), LocalDate.of(1990, 1, 1), "B2-1");

        assertThat(invalidFields(validator.validate(request))).containsExactly("licenceDate");
    }

    @Test
    @DisplayName("Ngày cấp GPLX khi chưa đủ 18 tuổi -> Lỗi tại ô licenceDate")
    void testLicenceBeforeAdulthood_ShouldRejectLicenceDate() {
        RegisterRequest request = register(LocalDate.of(1995, 5, 15), LocalDate.of(2012, 5, 14), "B2-1");

        assertThat(invalidFields(validator.validate(request))).containsExactly("licenceDate");
    }

    @Test
    @DisplayName("Số GPLX quá 20 ký tự -> Lỗi validation thay vì lỗi 500 khi insert")
    void testLicenceNumberTooLong_ShouldRejectLicenceNumber() {
        RegisterRequest request = register(LocalDate.of(1995, 5, 15), LocalDate.of(2020, 1, 10), "kljakdsljlksadjflkfjx");

        assertThat(invalidFields(validator.validate(request))).containsExactly("licenceNumber");
    }

    @Test
    @DisplayName("Hồ sơ khách hàng dùng chung rule ngày cấp GPLX")
    void testProfile_LicenceBeforeBirthday_ShouldRejectLicenceDate() {
        CustomerProfileRequest request = new CustomerProfileRequest("Nguyễn Văn A", "0901234567",
                LocalDate.of(1995, 5, 15), "001095012345", "B2-987654321", LocalDate.of(1994, 1, 1));

        assertThat(invalidFields(validator.validate(request))).containsExactly("licenceDate");
    }

    @Test
    @DisplayName("Đặt xe: ngày trả trước ngày nhận -> Lỗi returnDateAfterPickupDate")
    void testRental_ReturnBeforePickup_ShouldReject() {
        RentalCreateRequest request = new RentalCreateRequest(List.of(1),
                LocalDate.now().plusDays(3), LocalDate.now().plusDays(1));

        assertThat(invalidFields(validator.validate(request))).containsExactly("returnDateAfterPickupDate");
    }
}
