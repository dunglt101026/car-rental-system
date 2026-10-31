package com.hsf.fucarrenting.rental.controller;

import com.hsf.fucarrenting.auth.dto.AccountResponse;
import com.hsf.fucarrenting.car.dto.CarResponse;
import com.hsf.fucarrenting.car.service.CarServiceImpl;
import com.hsf.fucarrenting.car.entity.CarStatus;
import com.hsf.fucarrenting.common.config.AuthInterceptor;
import com.hsf.fucarrenting.common.exception.BusinessException;
import com.hsf.fucarrenting.customer.dto.CustomerResponse;
import com.hsf.fucarrenting.customer.service.CustomerService;
import com.hsf.fucarrenting.rental.dto.RentalCreateRequest;
import com.hsf.fucarrenting.rental.dto.RentalResponse;
import com.hsf.fucarrenting.rental.service.RentalService;
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
import java.util.stream.Collectors;

@Controller
@RequestMapping("/customer/rentals")
@RequiredArgsConstructor
@Slf4j
public class RentalController {

    private final RentalService rentalService;
    private final CarServiceImpl carService;
    private final CustomerService customerService;

    @GetMapping
    public String listRentals(@SessionAttribute(AuthInterceptor.SESSION_USER) AccountResponse currentUser, Model model) {
        CustomerResponse customer = customerService.getCustomerByAccountId(currentUser.accountId());
        List<RentalResponse> rentals = rentalService.getCustomerRentals(customer.customerId());

        model.addAttribute("customer", customer);
        model.addAttribute("rentals", rentals);

        return "rental/list";
    }

    @GetMapping("/book")
    public String showBookingForm(
            @RequestParam("carId") Integer carId,
            @SessionAttribute(AuthInterceptor.SESSION_USER) AccountResponse currentUser,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        CarResponse car = carService.getCarById(carId);
        if (!CarStatus.Available.name().equalsIgnoreCase(car.status())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Xe '" + car.carName() + "' hiện không khả dụng để đặt thuê.");
            return "redirect:/cars";
        }

        CustomerResponse customer = customerService.getCustomerByAccountId(currentUser.accountId());

        if (!model.containsAttribute("rentalRequest")) {
            RentalCreateRequest defaultForm = new RentalCreateRequest(
                    List.of(carId),
                    LocalDate.now(),
                    LocalDate.now().plusDays(1)
            );
            model.addAttribute("rentalRequest", defaultForm);
        }

        populateBookingModel(model, car, customer);

        return "rental/book";
    }

    @PostMapping("/book")
    public String handleBookCar(
            @Valid @ModelAttribute("rentalRequest") RentalCreateRequest request,
            BindingResult bindingResult,
            @SessionAttribute(AuthInterceptor.SESSION_USER) AccountResponse currentUser,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (request.carIds() == null || request.carIds().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn ít nhất một xe để đặt thuê.");
            return "redirect:/cars";
        }

        CustomerResponse customer = customerService.getCustomerByAccountId(currentUser.accountId());
        CarResponse car = carService.getCarById(request.carIds().getFirst());

        if (bindingResult.hasErrors()) {
            populateBookingModel(model, car, customer);
            return "rental/book";
        }

        try {
            List<RentalResponse> booked = rentalService.bookCars(customer.customerId(), request);
            String carNames = booked.stream().map(rental -> rental.carName()).collect(Collectors.joining(", "));
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đặt thuê thành công " + booked.size() + " xe: " + carNames + ".");
            return "redirect:/customer/rentals";
        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            populateBookingModel(model, car, customer);
            return "rental/book";
        }
    }

    private void populateBookingModel(Model model, CarResponse car, CustomerResponse customer) {
        List<CarResponse> otherCars = carService.getAvailableCars(null, null, null, null, null)
                .stream()
                .filter(c -> !c.carId().equals(car.carId()))
                .toList();

        model.addAttribute("car", car);
        model.addAttribute("otherCars", otherCars);
        model.addAttribute("customer", customer);
    }
}

