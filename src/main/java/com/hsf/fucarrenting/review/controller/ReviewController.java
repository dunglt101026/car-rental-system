package com.hsf.fucarrenting.review.controller;

import com.hsf.fucarrenting.auth.dto.AccountResponse;
import com.hsf.fucarrenting.common.config.AuthInterceptor;
import com.hsf.fucarrenting.common.exception.BusinessException;
import com.hsf.fucarrenting.customer.dto.CustomerResponse;
import com.hsf.fucarrenting.customer.service.CustomerService;
import com.hsf.fucarrenting.rental.dto.RentalResponse;
import com.hsf.fucarrenting.review.dto.ReviewCreateRequest;
import com.hsf.fucarrenting.review.service.ReviewService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;
    private final CustomerService customerService;

    @GetMapping("/new")
    public String showReviewForm(
            @RequestParam("carRentId") Integer carRentId,
            @SessionAttribute(AuthInterceptor.SESSION_USER) AccountResponse currentUser,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        CustomerResponse customer = customerService.getCustomerByAccountId(currentUser.accountId());
        try {
            RentalResponse rental = reviewService.getReviewableRental(customer.customerId(), carRentId);
            model.addAttribute("rental", rental);
            model.addAttribute("reviewRequest", new ReviewCreateRequest(carRentId, 5, ""));
            return "review/form";
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/customer/rentals";
        }
    }

    @PostMapping
    public String handleCreateReview(
            @Valid @ModelAttribute("reviewRequest") ReviewCreateRequest request,
            BindingResult bindingResult,
            @SessionAttribute(AuthInterceptor.SESSION_USER) AccountResponse currentUser,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (request.carRentId() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mã đơn thuê không được để trống.");
            return "redirect:/customer/rentals";
        }

        CustomerResponse customer = customerService.getCustomerByAccountId(currentUser.accountId());
        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute("rental", reviewService.getReviewableRental(customer.customerId(), request.carRentId()));
                return "review/form";
            }

            reviewService.createReview(customer.customerId(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã gửi đánh giá cho chuyến xe!");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/customer/rentals";
    }
}
