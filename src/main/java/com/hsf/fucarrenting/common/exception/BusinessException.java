package com.hsf.fucarrenting.common.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends AppException {
    public BusinessException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
