package com.hsf.fucarrenting.customer.service;

import com.hsf.fucarrenting.auth.Account;
import com.hsf.fucarrenting.auth.AccountRepository;
import com.hsf.fucarrenting.common.exception.BusinessException;
import com.hsf.fucarrenting.common.exception.ResourceNotFoundException;
import com.hsf.fucarrenting.customer.entity.Customer;
import com.hsf.fucarrenting.customer.mapper.CustomerMapper;
import com.hsf.fucarrenting.customer.repository.CustomerRepository;
import com.hsf.fucarrenting.customer.repository.CustomerSpecification;
import com.hsf.fucarrenting.customer.dto.CustomerProfileRequest;
import com.hsf.fucarrenting.customer.dto.CustomerResponse;
import com.hsf.fucarrenting.rental.repository.CarRentalRepository;
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
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final AccountRepository accountRepository;
    private final CarRentalRepository carRentalRepository;

    @Override
    public Page<CustomerResponse> getAdminCustomers(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "customerId"));
        Specification<Customer> spec = CustomerSpecification.filterCustomers(keyword);
        return customerRepository.findAll(spec, pageable).map(customer -> customerMapper.toResponse(customer));
    }

    @Override
    public CustomerResponse getCustomerByAccountId(Integer accountId) {
        Customer customer = getCustomerEntityByAccountId(accountId);
        return customerMapper.toResponse(customer);
    }

    @Override
    public Customer getCustomerEntityByAccountId(Integer accountId) {
        return customerRepository.findByAccount_AccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin khách hàng cho tài khoản: " + accountId));
    }

    @Override
    public CustomerResponse getCustomerById(Integer customerId) {
        Customer customer = getCustomerEntity(customerId);
        return customerMapper.toResponse(customer);
    }

    @Override
    public Customer getCustomerEntity(Integer customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin khách hàng với mã: " + customerId));
    }

    @Override
    public CustomerProfileRequest getCustomerFormById(Integer customerId) {
        Customer customer = getCustomerEntity(customerId);
        return customerMapper.toProfileRequest(customer);
    }

    @Transactional
    @Override
    public CustomerResponse updateProfile(Integer accountId, CustomerProfileRequest request) {
        Customer customer = getCustomerEntityByAccountId(accountId);
        return applyCustomerUpdate(customer, request);
    }

    @Transactional
    @Override
    public CustomerResponse updateCustomer(Integer customerId, CustomerProfileRequest request) {
        Customer customer = getCustomerEntity(customerId);
        return applyCustomerUpdate(customer, request);
    }

    @Override
    public CustomerResponse applyCustomerUpdate(Customer customer, CustomerProfileRequest request) {
        // Check duplicates with OTHER customers (exists query stays safe even if the DB already has duplicates)
        String mobile = request.mobile().trim();
        if (customerRepository.existsByMobileAndCustomerIdNot(mobile, customer.getCustomerId())) {
            throw new BusinessException("Số điện thoại '" + mobile + "' đã được sử dụng bởi khách hàng khác.");
        }

        String identityCard = request.identityCard().trim();
        if (customerRepository.existsByIdentityCardAndCustomerIdNot(identityCard, customer.getCustomerId())) {
            throw new BusinessException("Số CCCD/CMND '" + identityCard + "' đã được sử dụng bởi khách hàng khác.");
        }

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
    @Override
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
