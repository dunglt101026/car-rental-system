package com.hsf.asgm.review;

import com.hsf.asgm.auth.AccountResponse;
import com.hsf.asgm.common.config.AuthInterceptor;
import com.hsf.asgm.common.exception.BusinessException;
import com.hsf.asgm.customer.CustomerResponse;
import com.hsf.asgm.customer.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;
    private final CustomerService customerService;

    @PostMapping
    public String handleCreateReview(
            @Valid @ModelAttribute ReviewCreateRequest request,
            BindingResult bindingResult,
            @SessionAttribute(AuthInterceptor.SESSION_USER) AccountResponse currentUser,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            String error = bindingResult.getAllErrors().getFirst().getDefaultMessage();
            redirectAttributes.addFlashAttribute("errorMessage", error);
            return "redirect:/customer/rentals";
        }

        try {
            CustomerResponse customer = customerService.getCustomerByAccountId(currentUser.accountId());
            reviewService.createReview(customer.customerId(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã gửi đánh giá cho chuyến xe!");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/customer/rentals";
    }
}

