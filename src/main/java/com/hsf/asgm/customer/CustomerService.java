package com.hsf.asgm.customer;

import com.hsf.asgm.common.exception.BusinessException;
import com.hsf.asgm.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerResponse getCustomerByAccountId(Integer accountId) {
        Customer customer = getCustomerEntityByAccountId(accountId);
        return customerMapper.toResponse(customer);
    }

    public Customer getCustomerEntityByAccountId(Integer accountId) {
        return customerRepository.findByAccount_AccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin khách hàng cho tài khoản: " + accountId));
    }

    public CustomerResponse getCustomerById(Integer customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin khách hàng với mã: " + customerId));
        return customerMapper.toResponse(customer);
    }

    @Transactional
    public CustomerResponse updateProfile(Integer accountId, CustomerProfileRequest request) {
        Customer customer = getCustomerEntityByAccountId(accountId);

        // Check mobile duplicate
        customerRepository.findByMobile(request.mobile()).ifPresent(existing -> {
            if (!existing.getCustomerId().equals(customer.getCustomerId())) {
                throw new BusinessException("Số điện thoại '" + request.mobile() + "' đã được sử dụng bởi khách hàng khác.");
            }
        });

        // Check identity card duplicate
        customerRepository.findByIdentityCard(request.identityCard()).ifPresent(existing -> {
            if (!existing.getCustomerId().equals(customer.getCustomerId())) {
                throw new BusinessException("Số CCCD/CMND '" + request.identityCard() + "' đã được sử dụng bởi khách hàng khác.");
            }
        });

        customer.setFullName(request.fullName());
        customer.setMobile(request.mobile());
        customer.setBirthday(request.birthday());
        customer.setIdentityCard(request.identityCard());
        customer.setLicenceNumber(request.licenceNumber());
        customer.setLicenceDate(request.licenceDate());

        Customer saved = customerRepository.save(customer);
        log.info("Updated customer profile id={} accountId={}", saved.getCustomerId(), accountId);
        return customerMapper.toResponse(saved);
    }
}

