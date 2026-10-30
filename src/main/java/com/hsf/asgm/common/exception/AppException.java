package com.hsf.asgm.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base class for all application exceptions. The status decides which error page is rendered.
 */
@Getter
public abstract class AppException extends RuntimeException {

    private final HttpStatus status;

    protected AppException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
