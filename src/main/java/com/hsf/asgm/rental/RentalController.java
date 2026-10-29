package com.hsf.asgm.rental;

import com.hsf.asgm.auth.AccountResponse;
import com.hsf.asgm.car.CarResponse;
import com.hsf.asgm.car.CarService;
import com.hsf.asgm.common.config.AuthInterceptor;
import com.hsf.asgm.common.exception.BusinessException;
import com.hsf.asgm.customer.CustomerResponse;
import com.hsf.asgm.customer.CustomerService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/customer/rentals")
@RequiredArgsConstructor
@Slf4j
public class RentalController {

    private final RentalService rentalService;
    private final CarService carService;
    private final CustomerService customerService;

    @GetMapping
    public String listRentals(HttpSession session, Model model) {
        AccountResponse currentUser = (AccountResponse) session.getAttribute(AuthInterceptor.SESSION_USER);
        if (currentUser == null) {
            return "redirect:/login?error=login_required";
        }

        CustomerResponse customer = customerService.getCustomerByAccountId(currentUser.accountId());
        List<RentalResponse> rentals = rentalService.getCustomerRentals(customer.customerId());

        model.addAttribute("customer", customer);
        model.addAttribute("rentals", rentals);

        return "rental/list";
    }

    @GetMapping("/book")
    public String showBookingForm(
            @RequestParam("carId") Integer carId,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        AccountResponse currentUser = (AccountResponse) session.getAttribute(AuthInterceptor.SESSION_USER);
        if (currentUser == null) {
            return "redirect:/login?error=login_required";
        }

        CarResponse car = carService.getCarById(carId);
        if (!"Available".equalsIgnoreCase(car.status())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Xe '" + car.carName() + "' hiện không khả dụng để đặt thuê.");
            return "redirect:/cars";
        }

        CustomerResponse customer = customerService.getCustomerByAccountId(currentUser.accountId());

        if (!model.containsAttribute("rentalRequest")) {
            RentalCreateRequest defaultForm = new RentalCreateRequest(
                    carId,
                    LocalDate.now(),
                    LocalDate.now().plusDays(1)
            );
            model.addAttribute("rentalRequest", defaultForm);
        }

        model.addAttribute("car", car);
        model.addAttribute("customer", customer);

        return "rental/book";
    }

    @PostMapping("/book")
    public String handleBookCar(
            @Valid @ModelAttribute("rentalRequest") RentalCreateRequest request,
            BindingResult bindingResult,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        AccountResponse currentUser = (AccountResponse) session.getAttribute(AuthInterceptor.SESSION_USER);
        if (currentUser == null) {
            return "redirect:/login?error=login_required";
        }

        CustomerResponse customer = customerService.getCustomerByAccountId(currentUser.accountId());
        CarResponse car = carService.getCarById(request.carId());

        if (bindingResult.hasErrors()) {
            model.addAttribute("car", car);
            model.addAttribute("customer", customer);
            return "rental/book";
        }

        try {
            rentalService.bookCar(customer.customerId(), request);
            redirectAttributes.addFlashAttribute("successMessage", "Đặt thuê xe '" + car.carName() + "' thành công!");
            return "redirect:/customer/rentals";
        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("car", car);
            model.addAttribute("customer", customer);
            return "rental/book";
        }
    }
}

