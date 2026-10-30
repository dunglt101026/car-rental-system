package com.hsf.asgm.customer;

import com.hsf.asgm.auth.Account;
import com.hsf.asgm.auth.AccountRepository;
import com.hsf.asgm.common.exception.BusinessException;
import com.hsf.asgm.common.exception.ResourceNotFoundException;
import com.hsf.asgm.rental.CarRentalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final AccountRepository accountRepository;
    private final CarRentalRepository carRentalRepository;

    public Page<CustomerResponse> getAdminCustomers(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "customerId"));
        Specification<Customer> spec = CustomerSpecification.filterCustomers(keyword);
        return customerRepository.findAll(spec, pageable).map(customer -> customerMapper.toResponse(customer));
    }

    public CustomerResponse getCustomerByAccountId(Integer accountId) {
        Customer customer = getCustomerEntityByAccountId(accountId);
        return customerMapper.toResponse(customer);
    }

    public Customer getCustomerEntityByAccountId(Integer accountId) {
        return customerRepository.findByAccount_AccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin khách hàng cho tài khoản: " + accountId));
    }

    public CustomerResponse getCustomerById(Integer customerId) {
        Customer customer = getCustomerEntity(customerId);
        return customerMapper.toResponse(customer);
    }

    public Customer getCustomerEntity(Integer customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin khách hàng với mã: " + customerId));
    }

    public CustomerProfileRequest getCustomerFormById(Integer customerId) {
        Customer customer = getCustomerEntity(customerId);
        return customerMapper.toProfileRequest(customer);
    }

    @Transactional
    public CustomerResponse updateProfile(Integer accountId, CustomerProfileRequest request) {
        Customer customer = getCustomerEntityByAccountId(accountId);
        return applyCustomerUpdate(customer, request);
    }

    @Transactional
    public CustomerResponse updateCustomer(Integer customerId, CustomerProfileRequest request) {
        Customer customer = getCustomerEntity(customerId);
        return applyCustomerUpdate(customer, request);
    }

    private CustomerResponse applyCustomerUpdate(Customer customer, CustomerProfileRequest request) {
        // Check mobile duplicate with other customers
        customerRepository.findByMobile(request.mobile().trim()).ifPresent(existing -> {
            if (!existing.getCustomerId().equals(customer.getCustomerId())) {
                throw new BusinessException("Số điện thoại '" + request.mobile().trim() + "' đã được sử dụng bởi khách hàng khác.");
            }
        });

        // Check identity card duplicate with other customers
        customerRepository.findByIdentityCard(request.identityCard().trim()).ifPresent(existing -> {
            if (!existing.getCustomerId().equals(customer.getCustomerId())) {
                throw new BusinessException("Số CCCD/CMND '" + request.identityCard().trim() + "' đã được sử dụng bởi khách hàng khác.");
            }
        });

        customerMapper.updateEntityFromProfile(request, customer);

        Customer saved = customerRepository.save(customer);
        log.info("Updated customer profile id={}", saved.getCustomerId());
        return customerMapper.toResponse(saved);
    }

    /**
     * Deletes a customer together with the linked login account.
     * Customers who already have rental transactions cannot be deleted.
     */
    @Transactional
    public String deleteCustomer(Integer customerId) {
        Customer customer = getCustomerEntity(customerId);

        if (carRentalRepository.existsByCustomer_CustomerId(customerId)) {
            throw new BusinessException("Không thể xóa khách hàng \"" + customer.getFullName()
                    + "\" vì đã phát sinh giao dịch thuê xe.");
        }

        Account account = customer.getAccount();
        customerRepository.delete(customer);
        if (account != null) {
            accountRepository.delete(account);
        }

        log.info("Deleted customer id={} and linked account", customerId);
        return customer.getFullName();
    }
}
