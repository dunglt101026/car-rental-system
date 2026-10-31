package com.hsf.fucarrenting.common.exception;

import com.hsf.fucarrenting.common.config.AuthInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public String handleAppException(AppException ex, Model model,
                                     HttpServletRequest request, HttpServletResponse response) {
        log.warn("{} ({}): {}", ex.getClass().getSimpleName(), ex.getStatus().value(), ex.getMessage());
        return renderError(ex.getStatus(), ex.getMessage(), model, request, response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNoResourceFound(NoResourceFoundException ex, Model model,
                                        HttpServletRequest request, HttpServletResponse response) {
        log.warn("Route or static resource not found: {}", ex.getMessage());
        return renderError(HttpStatus.NOT_FOUND, "Đường dẫn không tồn tại trên hệ thống.", model, request, response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, Model model,
                                                   HttpServletRequest request, HttpServletResponse response) {
        log.warn("Parameter type mismatch: name={}, value={}", ex.getName(), ex.getValue());
        return renderError(HttpStatus.NOT_FOUND,
                "Tham số yêu cầu không đúng định dạng hoặc tài nguyên không tồn tại.", model, request, response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public String handleMissingParams(MissingServletRequestParameterException ex, Model model,
                                      HttpServletRequest request, HttpServletResponse response) {
        log.warn("Missing request parameter: {}", ex.getParameterName());
        return renderError(HttpStatus.BAD_REQUEST,
                "Thiếu tham số bắt buộc: " + ex.getParameterName(), model, request, response);
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model,
                                         HttpServletRequest request, HttpServletResponse response) {
        log.error("Internal Server Error: ", ex);
        return renderError(HttpStatus.INTERNAL_SERVER_ERROR,
                "Đã có sự cố bất ngờ xảy ra phía máy chủ. Vui lòng thử lại sau.", model, request, response);
    }

    private String renderError(HttpStatus status, String message, Model model,
                               HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(status.value());
        model.addAttribute("errorMessage", message);

        // @ModelAttribute methods of GlobalModelAttributes do not run for exception handlers,
        // so the logged-in user must be added here for the navbar to render correctly.
        HttpSession session = request.getSession(false);
        if (session != null) {
            model.addAttribute("currentUser", session.getAttribute(AuthInterceptor.SESSION_USER));
        }

        return switch (status) {
            case NOT_FOUND -> "error/404";
            case FORBIDDEN -> "error/403";
            case INTERNAL_SERVER_ERROR -> "error/500";
            default -> "error/400";
        };
    }
}
