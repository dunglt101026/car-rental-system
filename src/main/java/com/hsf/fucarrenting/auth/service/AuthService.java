package com.hsf.fucarrenting.auth.service;

import com.hsf.fucarrenting.auth.dto.AccountResponse;
import com.hsf.fucarrenting.auth.dto.RegisterRequest;
import org.springframework.transaction.annotation.Transactional;

public interface AuthService {
    @Transactional(readOnly = true)
    AccountResponse login(String identifier, String password);

    @Transactional
    AccountResponse register(RegisterRequest request);
}
