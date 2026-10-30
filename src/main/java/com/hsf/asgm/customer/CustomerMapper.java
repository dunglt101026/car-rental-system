package com.hsf.asgm.customer;

import com.hsf.asgm.auth.Account;
import com.hsf.asgm.auth.RegisterRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerMapper {

    @Mapping(target = "accountId", source = "account.accountId")
    @Mapping(target = "accountName", source = "account.accountName")
    @Mapping(target = "email", source = "account.email")
    CustomerResponse toResponse(Customer customer);

    CustomerProfileRequest toProfileRequest(Customer customer);

    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "carRentals", ignore = true)
    void updateEntityFromProfile(CustomerProfileRequest request, @MappingTarget Customer customer);

    @Mapping(target = "customerId", ignore = true)
    @Mapping(target = "carRentals", ignore = true)
    @Mapping(target = "account", source = "account")
    Customer toEntity(RegisterRequest request, Account account);
}
