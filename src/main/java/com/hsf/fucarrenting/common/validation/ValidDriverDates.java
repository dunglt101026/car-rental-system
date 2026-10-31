package com.hsf.fucarrenting.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Business rule between birthday and licence date.
 * Errors are attached to the birthday / licenceDate fields so the form shows them under the input.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DriverDatesValidator.class)
public @interface ValidDriverDates {
    String message() default "Ngày sinh hoặc ngày cấp GPLX không hợp lệ";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
