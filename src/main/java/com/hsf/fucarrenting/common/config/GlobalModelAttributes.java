package com.hsf.fucarrenting.common.config;

import com.hsf.fucarrenting.auth.AccountResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("currentUser")
    public AccountResponse currentUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (AccountResponse) session.getAttribute(AuthInterceptor.SESSION_USER);
    }
}
