package com.hsf.asgm.customer;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerMapper {

    @Mapping(target = "accountId", source = "account.accountId")
    @Mapping(target = "accountName", source = "account.accountName")
    @Mapping(target = "email", source = "account.email")
    CustomerResponse toResponse(Customer customer);
}

