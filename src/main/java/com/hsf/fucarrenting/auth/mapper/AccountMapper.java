package com.hsf.fucarrenting.auth.mapper;

import com.hsf.fucarrenting.auth.dto.AccountResponse;
import com.hsf.fucarrenting.auth.dto.RegisterRequest;
import com.hsf.fucarrenting.auth.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountMapper {

    @Mapping(target = "customerId", source = "customer.customerId")
    @Mapping(target = "fullName", source = "customer.fullName")
    AccountResponse toResponse(Account account);

    /**
     * Builds a new Customer-role account. The password must already be encoded.
     */
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "password", source = "encodedPassword")
    @Mapping(target = "role", constant = "Customer")
    Account toCustomerAccount(RegisterRequest request, String encodedPassword);
}
