package com.hsf.fucarrenting.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

/**
 * - A renter must be between 18 and 100 years old (a class B licence requires age 18).
 * - The licence date must be at least 18 years after the birthday.
 * Future dates are reported by @Past / @PastOrPresent on the fields, so they are skipped here.
 */
public class DriverDatesValidator implements ConstraintValidator<ValidDriverDates, DriverDates> {

    static final int MIN_AGE = 18;
    static final int MAX_AGE = 100;

    @Override
    public boolean isValid(DriverDates value, ConstraintValidatorContext context) {
        if (value == null || value.birthday() == null) {
            return true;
        }

        LocalDate today = LocalDate.now();
        LocalDate birthday = value.birthday();
        LocalDate licenceDate = value.licenceDate();
        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if (birthday.isBefore(today)) {
            if (birthday.isAfter(today.minusYears(MIN_AGE))) {
                reject(context, "birthday", "Khách thuê xe phải đủ " + MIN_AGE + " tuổi");
                valid = false;
            } else if (birthday.isBefore(today.minusYears(MAX_AGE))) {
                reject(context, "birthday", "Ngày sinh không hợp lệ (quá " + MAX_AGE + " tuổi)");
                valid = false;
            }
        }

        if (licenceDate != null && !licenceDate.isAfter(today)
                && licenceDate.isBefore(birthday.plusYears(MIN_AGE))) {
            reject(context, "licenceDate", "Ngày cấp GPLX phải sau ngày sinh ít nhất " + MIN_AGE + " năm (đủ tuổi thi bằng lái)");
            valid = false;
        }

        return valid;
    }

    private void reject(ConstraintValidatorContext context, String field, String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(field)
                .addConstraintViolation();
    }
}
