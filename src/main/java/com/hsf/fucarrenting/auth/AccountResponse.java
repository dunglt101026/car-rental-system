package com.hsf.fucarrenting.auth;

import java.io.Serializable;

public record AccountResponse(
        Integer accountId,
        String accountName,
        String email,
        String role,
        Integer customerId,
        String fullName
) implements Serializable {}
