package com.hsf.fucarrenting.auth.service;

import com.hsf.fucarrenting.auth.dto.AccountResponse;
import com.hsf.fucarrenting.auth.dto.RegisterRequest;
import com.hsf.fucarrenting.auth.entity.Account;
import com.hsf.fucarrenting.auth.entity.Role;
import com.hsf.fucarrenting.auth.mapper.AccountMapper;
import com.hsf.fucarrenting.auth.repository.AccountRepository;
import com.hsf.fucarrenting.common.exception.BusinessException;
import com.hsf.fucarrenting.customer.entity.Customer;
import com.hsf.fucarrenting.customer.mapper.CustomerMapper;
import com.hsf.fucarrenting.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final AccountMapper accountMapper;
    private final CustomerMapper customerMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    @Override
    public AccountResponse login(String identifier, String password) {
        if (identifier == null || identifier.isBlank() || password == null || password.isBlank()) {
            throw new BusinessException("Vui lòng nhập đầy đủ thông tin đăng nhập.");
        }

        String trimmedId = identifier.trim();
        // Duplicated rows (data typed straight into the DB) must not crash login: take the one whose password matches
        Account account = accountRepository.findByAccountNameOrEmailOrderByAccountIdAsc(trimmedId, trimmedId)
                .stream()
                .filter(candidate -> passwordEncoder.matches(password, candidate.getPassword()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Tên đăng nhập/email hoặc mật khẩu không chính xác."));

        // A Customer account without a customer profile cannot use any customer page
        if (account.getRole() == Role.Customer && account.getCustomer() == null) {
            throw new BusinessException("Tài khoản chưa có hồ sơ khách hàng. Vui lòng liên hệ quản trị viên.");
        }

        return accountMapper.toResponse(account);
    }

    @Transactional
    @Override
    public AccountResponse register(RegisterRequest request) {
        if (accountRepository.existsByAccountName(request.accountName())) {
            throw new BusinessException("Tên đăng nhập '" + request.accountName() + "' đã được sử dụng.");
        }
        if (accountRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email '" + request.email() + "' đã được đăng ký.");
        }
        if (customerRepository.existsByIdentityCard(request.identityCard())) {
            throw new BusinessException("Số CCCD/CMND '" + request.identityCard() + "' đã tồn tại trong hệ thống.");
        }
        if (customerRepository.existsByMobile(request.mobile())) {
            throw new BusinessException("Số điện thoại '" + request.mobile() + "' đã được đăng ký.");
        }

        Account account = accountMapper.toCustomerAccount(request, passwordEncoder.encode(request.password()));
        Account savedAccount = accountRepository.save(account);

        Customer customer = customerMapper.toEntity(request, savedAccount);
        customerRepository.save(customer);
        savedAccount.setCustomer(customer);

        log.info("Customer account registered successfully: username={}, email={}", savedAccount.getAccountName(), savedAccount.getEmail());
        return accountMapper.toResponse(savedAccount);
    }
}

