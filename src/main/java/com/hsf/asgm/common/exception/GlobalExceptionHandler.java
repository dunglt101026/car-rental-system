package com.hsf.asgm.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(ResourceNotFoundException ex, Model model) {
        log.warn("Resource not found: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNoResourceFound(NoResourceFoundException ex, Model model) {
        log.warn("Route or static resource not found: {}", ex.getMessage());
        model.addAttribute("errorMessage", "Đường dẫn không tồn tại trên hệ thống.");
        return "error/404";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, Model model) {
        log.warn("Parameter type mismatch: name={}, value={}", ex.getName(), ex.getValue());
        model.addAttribute("errorMessage", "Tham số yêu cầu không đúng định dạng hoặc tài nguyên không tồn tại.");
        return "error/404";
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public String handleMissingParams(MissingServletRequestParameterException ex, Model model) {
        log.warn("Missing request parameter: {}", ex.getParameterName());
        model.addAttribute("errorMessage", "Thiếu tham số bắt buộc: " + ex.getParameterName());
        return "error/400";
    }

    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(BusinessException ex, Model model) {
        log.warn("Business rule violation: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/400";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {
        log.error("Internal Server Error: ", ex);
        model.addAttribute("errorMessage", "Đã có sự cố bất ngờ xảy ra phía máy chủ. Vui lòng thử lại sau.");
        return "error/500";
    }
}

