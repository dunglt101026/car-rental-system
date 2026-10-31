package com.hsf.fucarrenting.common.config;

import com.hsf.fucarrenting.auth.dto.AccountResponse;
import com.hsf.fucarrenting.auth.entity.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class AuthInterceptor implements HandlerInterceptor {

    public static final String SESSION_USER = "CURRENT_USER";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        HttpSession session = request.getSession(false);
        AccountResponse currentUser = (session != null) ? (AccountResponse) session.getAttribute(SESSION_USER) : null;

        // Admin paths protection
        if (uri.startsWith("/admin")) {
            if (currentUser == null) {
                log.info("Unauthenticated access to admin path '{}'. Redirecting to /login", uri);
                response.sendRedirect(request.getContextPath() + "/login?error=unauthorized");
                return false;
            }
            if (!Role.Admin.name().equalsIgnoreCase(currentUser.role())) {
                log.warn("Forbidden access: User '{}' with role '{}' tried to access admin route '{}'",
                        currentUser.accountName(), currentUser.role(), uri);
                response.sendRedirect(request.getContextPath() + "/403");
                return false;
            }
        }

        // Customer paths protection
        if (uri.startsWith("/customer")) {
            if (currentUser == null) {
                log.info("Unauthenticated access to customer path '{}'. Redirecting to /login", uri);
                response.sendRedirect(request.getContextPath() + "/login?error=login_required");
                return false;
            }
            if (!Role.Customer.name().equalsIgnoreCase(currentUser.role())) {
                if (Role.Admin.name().equalsIgnoreCase(currentUser.role())) {
                    log.info("Admin user accessed customer path '{}'. Redirecting to /admin/cars", uri);
                    response.sendRedirect(request.getContextPath() + "/admin/cars");
                    return false;
                }
                log.warn("Forbidden access: Role '{}' tried to access customer route '{}'", currentUser.role(), uri);
                response.sendRedirect(request.getContextPath() + "/403");
                return false;
            }
        }


        return true;
    }
}

