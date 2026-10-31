package com.hsf.fucarrenting.common.validation;

import java.time.LocalDate;

/**
 * A form carrying the renter's birthday and driving licence date (register, profile, admin create/edit).
 */
public interface DriverDates {
    LocalDate birthday();

    LocalDate licenceDate();
}
