package com.hsf.asgm.auth;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountMapper {

    @Mapping(target = "customerId", source = "customer.customerId")
    @Mapping(target = "fullName", source = "customer.fullName")
    AccountResponse toResponse(Account account);
}
