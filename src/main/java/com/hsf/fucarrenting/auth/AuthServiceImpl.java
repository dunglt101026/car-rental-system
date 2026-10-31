package com.hsf.fucarrenting.auth;

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
        Account account = accountRepository.findByAccountName(trimmedId)
                .or(() -> accountRepository.findByEmail(trimmedId))
                .orElseThrow(() -> new BusinessException("Tên đăng nhập/email hoặc mật khẩu không chính xác."));

        if (!passwordEncoder.matches(password, account.getPassword())) {
            throw new BusinessException("Tên đăng nhập/email hoặc mật khẩu không chính xác.");
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

