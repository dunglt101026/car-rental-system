package com.hsf.fucarrenting.auth;

import com.hsf.fucarrenting.common.persistence.CaseInsensitiveEnumConverter;
import jakarta.persistence.Converter;

@Converter
public class RoleConverter extends CaseInsensitiveEnumConverter<Role> {
    public RoleConverter() {
        super(Role.class);
    }
}
