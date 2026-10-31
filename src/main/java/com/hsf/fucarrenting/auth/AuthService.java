package com.hsf.fucarrenting.auth;

import org.springframework.transaction.annotation.Transactional;

public interface AuthService {
    @Transactional(readOnly = true)
    AccountResponse login(String identifier, String password);

    @Transactional
    AccountResponse register(RegisterRequest request);
}
