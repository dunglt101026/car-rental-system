package com.hsf.asgm.customer;

import com.hsf.asgm.auth.AccountResponse;
import com.hsf.asgm.common.config.AuthInterceptor;
import com.hsf.asgm.common.exception.BusinessException;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping({"", "/dashboard"})
    public String customerDashboard(HttpSession session) {
        return "redirect:/customer/rentals";
    }

    @GetMapping("/profile")
    public String showProfilePage(HttpSession session, Model model) {
        AccountResponse currentUser = (AccountResponse) session.getAttribute(AuthInterceptor.SESSION_USER);
        if (currentUser == null) {
            return "redirect:/login?error=login_required";
        }

        CustomerResponse customer = customerService.getCustomerByAccountId(currentUser.accountId());
        model.addAttribute("customer", customer);

        if (!model.containsAttribute("profileRequest")) {
            CustomerProfileRequest form = new CustomerProfileRequest(
                    customer.fullName(),
                    customer.mobile(),
                    customer.birthday(),
                    customer.identityCard(),
                    customer.licenceNumber(),
                    customer.licenceDate()
            );
            model.addAttribute("profileRequest", form);
        }

        return "customer/profile";
    }

    @PostMapping("/profile")
    public String handleUpdateProfile(
            @Valid @ModelAttribute("profileRequest") CustomerProfileRequest request,
            BindingResult bindingResult,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        AccountResponse currentUser = (AccountResponse) session.getAttribute(AuthInterceptor.SESSION_USER);
        if (currentUser == null) {
            return "redirect:/login?error=login_required";
        }

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

