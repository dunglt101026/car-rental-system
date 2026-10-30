package com.hsf.asgm.home;

import com.hsf.asgm.auth.AccountResponse;
import com.hsf.asgm.common.config.AuthInterceptor;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class HomeController {

    @GetMapping({"/", "/home"})
    public String home(HttpSession session, Model model) {
        AccountResponse currentUser = (AccountResponse) session.getAttribute(AuthInterceptor.SESSION_USER);
        if (currentUser != null) {
            model.addAttribute("username", currentUser.accountName());
            model.addAttribute("currentUser", currentUser);
        }
        return "home";
    }

    @GetMapping("/403")
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String accessDenied() {
        return "error/403";
    }
}


