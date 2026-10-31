package com.hsf.fucarrenting.customer.controller;

import com.hsf.fucarrenting.auth.dto.AccountResponse;
import com.hsf.fucarrenting.common.config.AuthInterceptor;
import com.hsf.fucarrenting.common.exception.BusinessException;
import com.hsf.fucarrenting.customer.service.CustomerService;
import com.hsf.fucarrenting.customer.dto.CustomerProfileRequest;
import com.hsf.fucarrenting.customer.dto.CustomerResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public String customerRoot() {
        return "redirect:/customer/rentals";
    }

    @GetMapping("/profile")
    public String showProfilePage(@SessionAttribute(AuthInterceptor.SESSION_USER) AccountResponse currentUser, Model model) {
        CustomerResponse customer = customerService.getCustomerByAccountId(currentUser.accountId());
        model.addAttribute("customer", customer);

        if (!model.containsAttribute("profileRequest")) {
            model.addAttribute("profileRequest", customerService.getCustomerFormById(customer.customerId()));
        }

        return "customer/profile";
    }

    @PostMapping("/profile")
    public String handleUpdateProfile(
            @Valid @ModelAttribute("profileRequest") CustomerProfileRequest request,
            BindingResult bindingResult,
            @SessionAttribute(AuthInterceptor.SESSION_USER) AccountResponse currentUser,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            CustomerResponse customer = customerService.getCustomerByAccountId(currentUser.accountId());
            model.addAttribute("customer", customer);
            return "customer/profile";
        }

        try {
            customerService.updateProfile(currentUser.accountId(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ cá nhân thành công!");
            return "redirect:/customer/profile";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("profileRequest", request);
            return "redirect:/customer/profile";
        }
    }
}

