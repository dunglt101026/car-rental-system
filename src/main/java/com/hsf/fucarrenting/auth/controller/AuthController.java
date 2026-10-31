package com.hsf.fucarrenting.auth.controller;

import com.hsf.fucarrenting.auth.dto.AccountResponse;
import com.hsf.fucarrenting.auth.service.AuthService;
import com.hsf.fucarrenting.auth.dto.RegisterRequest;
import com.hsf.fucarrenting.auth.entity.Role;
import com.hsf.fucarrenting.common.config.AuthInterceptor;
import com.hsf.fucarrenting.common.exception.BusinessException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public String showLoginPage(
            @RequestParam(value = "error", required = false) String error,
            HttpSession session,
            Model model
    ) {
        AccountResponse currentUser = (AccountResponse) session.getAttribute(AuthInterceptor.SESSION_USER);
        if (currentUser != null) {
            if (Role.Admin.name().equalsIgnoreCase(currentUser.role())) {
                return "redirect:/admin/cars";
            }
            return "redirect:/cars";
        }

        if ("unauthorized".equals(error)) {
            model.addAttribute("errorMessage", "Vui lòng đăng nhập với tài khoản Quản trị viên để tiếp tục.");
        } else if ("login_required".equals(error)) {
            model.addAttribute("errorMessage", "Vui lòng đăng nhập để thực hiện chức năng này.");
        }

        return "login";
    }

    @PostMapping("/login")
    public String handleLogin(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        try {
            AccountResponse account = authService.login(username, password);
            session.setAttribute(AuthInterceptor.SESSION_USER, account);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng nhập thành công! Chào mừng " + account.accountName() + ".");

            if (Role.Admin.name().equalsIgnoreCase(account.role())) {
                return "redirect:/admin/cars";
            }
            return "redirect:/cars";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/register")
    public String showRegisterPage(HttpSession session, Model model) {
        AccountResponse currentUser = (AccountResponse) session.getAttribute(AuthInterceptor.SESSION_USER);
        if (currentUser != null) {
            return "redirect:/";
        }

        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest(
                    "", "", "", "", "", null, "", "", null
            ));
        }
        return "register";
    }

    @PostMapping("/register")
    public String handleRegister(
            @Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            authService.register(request);
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký tài khoản thành công! Vui lòng đăng nhập.");
            return "redirect:/login";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("registerRequest", request);
            return "redirect:/register";
        }
    }

    @PostMapping("/logout")
    public String handleLogout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute(AuthInterceptor.SESSION_USER);
        session.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", "Bạn đã đăng xuất khỏi hệ thống.");
        return "redirect:/login";
    }
}

