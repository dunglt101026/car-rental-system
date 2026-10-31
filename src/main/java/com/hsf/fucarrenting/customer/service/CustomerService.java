package com.hsf.fucarrenting.customer.service;

import com.hsf.fucarrenting.customer.entity.Customer;
import com.hsf.fucarrenting.customer.dto.CustomerProfileRequest;
import com.hsf.fucarrenting.customer.dto.CustomerResponse;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

public interface CustomerService {
    Page<CustomerResponse> getAdminCustomers(String keyword, int page, int size);

    CustomerResponse getCustomerByAccountId(Integer accountId);

    Customer getCustomerEntityByAccountId(Integer accountId);

    CustomerResponse getCustomerById(Integer customerId);

    Customer getCustomerEntity(Integer customerId);

    CustomerProfileRequest getCustomerFormById(Integer customerId);

    @Transactional
    CustomerResponse updateProfile(Integer accountId, CustomerProfileRequest request);

    @Transactional
    CustomerResponse updateCustomer(Integer customerId, CustomerProfileRequest request);

    CustomerResponse applyCustomerUpdate(Customer customer, CustomerProfileRequest request);

    @Transactional
    String deleteCustomer(Integer customerId);
}
