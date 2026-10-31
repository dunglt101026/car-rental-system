package com.hsf.asgm.auth;

import com.hsf.asgm.common.persistence.CaseInsensitiveEnumConverter;
import jakarta.persistence.Converter;

@Converter
public class RoleConverter extends CaseInsensitiveEnumConverter<Role> {
    public RoleConverter() {
        super(Role.class);
    }
}
