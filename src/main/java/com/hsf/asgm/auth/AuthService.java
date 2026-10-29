package com.hsf.asgm.auth;

import com.hsf.asgm.common.exception.BusinessException;
import com.hsf.asgm.customer.Customer;
import com.hsf.asgm.customer.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public AccountResponse login(String identifier, String password) {
        if (identifier == null || identifier.isBlank() || password == null || password.isBlank()) {
            throw new BusinessException("Vui lòng nhập đầy đủ thông tin đăng nhập.");
        }

        String trimmedId = identifier.trim();
        Account account = accountRepository.findByAccountName(trimmedId)
                .or(() -> accountRepository.findByEmail(trimmedId))
                .orElseThrow(() -> new BusinessException("Tên đăng nhập/email hoặc mật khẩu không chính xác."));

        boolean matched = false;
        String storedPassword = account.getPassword();
        if (storedPassword != null) {
            if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
                matched = passwordEncoder.matches(password, storedPassword);
            } else {
                matched = password.equals(storedPassword);
            }
        }

        if (!matched) {
            throw new BusinessException("Tên đăng nhập/email hoặc mật khẩu không chính xác.");
        }

        return accountMapper.toResponse(account);
    }

    @Transactional
    public AccountResponse register(RegisterRequest request) {
        if (accountRepository.existsByAccountName(request.accountName().trim())) {
            throw new BusinessException("Tên đăng nhập '" + request.accountName() + "' đã được sử dụng.");
        }
        if (accountRepository.existsByEmail(request.email().trim())) {
            throw new BusinessException("Email '" + request.email() + "' đã được đăng ký.");
        }
        if (customerRepository.existsByIdentityCard(request.identityCard().trim())) {
            throw new BusinessException("Số CCCD/CMND '" + request.identityCard() + "' đã tồn tại trong hệ thống.");
        }
        if (customerRepository.existsByMobile(request.mobile().trim())) {
            throw new BusinessException("Số điện thoại '" + request.mobile() + "' đã được đăng ký.");
        }

        Account account = Account.builder()
                .accountName(request.accountName().trim())
                .email(request.email().trim())
                .password(request.password())
                .role(Role.Customer.name())
                .build();

        Account savedAccount = accountRepository.save(account);

        Customer customer = Customer.builder()
                .fullName(request.fullName().trim())
                .mobile(request.mobile().trim())
                .birthday(request.birthday())
                .identityCard(request.identityCard().trim())
                .licenceNumber(request.licenceNumber().trim())
                .licenceDate(request.licenceDate())
                .account(savedAccount)
                .build();

        customerRepository.save(customer);
        savedAccount.setCustomer(customer);

        log.info("Customer account registered successfully: username={}, email={}", savedAccount.getAccountName(), savedAccount.getEmail());
        return accountMapper.toResponse(savedAccount);
    }
}

