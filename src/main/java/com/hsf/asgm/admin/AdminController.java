package com.hsf.asgm.admin;

import com.hsf.asgm.auth.AccountResponse;
import com.hsf.asgm.common.config.AuthInterceptor;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    @GetMapping("")
    public String adminRoot() {
        return "redirect:/admin/cars";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        AccountResponse currentUser = (AccountResponse) session.getAttribute(AuthInterceptor.SESSION_USER);
        if (currentUser != null) {
            model.addAttribute("adminName", currentUser.accountName());
        }
        model.addAttribute("activeTab", "dashboard");
        return "admin/dashboard";
    }
}
