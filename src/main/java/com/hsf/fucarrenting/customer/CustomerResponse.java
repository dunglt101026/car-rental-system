package com.hsf.fucarrenting.customer;

import java.time.LocalDate;

public record CustomerResponse(
        Integer customerId,
        String fullName,
        String mobile,
        LocalDate birthday,
        String identityCard,
        String licenceNumber,
        LocalDate licenceDate,
        Integer accountId,
        String accountName,
        String email
) {}

