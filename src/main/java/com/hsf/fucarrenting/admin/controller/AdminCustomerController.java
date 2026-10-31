package com.hsf.fucarrenting.admin.controller;

import com.hsf.fucarrenting.auth.AuthService;
import com.hsf.fucarrenting.auth.RegisterRequest;
import com.hsf.fucarrenting.common.exception.BusinessException;
import com.hsf.fucarrenting.customer.dto.CustomerProfileRequest;
import com.hsf.fucarrenting.customer.dto.CustomerResponse;
import com.hsf.fucarrenting.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/customers")
@RequiredArgsConstructor
@Slf4j
public class AdminCustomerController {

    private final CustomerService customerService;
    private final AuthService authService;

    @GetMapping
    public String listCustomers(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Model model
    ) {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;

        Page<CustomerResponse> customersPage = customerService.getAdminCustomers(keyword, page, size);

        model.addAttribute("customersPage", customersPage);
        model.addAttribute("customers", customersPage.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", customersPage.getTotalPages());
        model.addAttribute("totalElements", customersPage.getTotalElements());
        model.addAttribute("activeTab", "customers");

        return "admin/customer/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest(
                    "", "", "", "", "", null, "", "", null
            ));
        }
        model.addAttribute("activeTab", "customers");
        return "admin/customer/create";
    }

    @PostMapping("/new")
    public String handleCreateCustomer(
            @Valid @ModelAttribute("registerRequest") RegisterRequest registerRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        model.addAttribute("activeTab", "customers");
        if (bindingResult.hasErrors()) {
            return "admin/customer/create";
        }

        try {
            authService.register(registerRequest);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Thêm khách hàng \"" + registerRequest.fullName().trim() + "\" thành công!");
            return "redirect:/admin/customers";
        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "admin/customer/create";
        }
    }

    @PostMapping("/delete/{id}")
    public String handleDeleteCustomer(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            String fullName = customerService.deleteCustomer(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã xóa khách hàng \"" + fullName + "\" (Mã: #" + id + ") cùng tài khoản đăng nhập.");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/customers";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        CustomerResponse customer = customerService.getCustomerById(id);
        if (!model.containsAttribute("profileRequest")) {
            CustomerProfileRequest form = customerService.getCustomerFormById(id);
            model.addAttribute("profileRequest", form);
        }
        model.addAttribute("customer", customer);
        model.addAttribute("customerId", id);
        model.addAttribute("activeTab", "customers");

        return "admin/customer/edit";
    }

    @PostMapping("/edit/{id}")
    public String handleUpdateCustomer(
            @PathVariable("id") Integer id,
            @Valid @ModelAttribute("profileRequest") CustomerProfileRequest profileRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            CustomerResponse customer = customerService.getCustomerById(id);
            model.addAttribute("customer", customer);
            model.addAttribute("customerId", id);
            model.addAttribute("activeTab", "customers");
            return "admin/customer/edit";
        }

        try {
            CustomerResponse updated = customerService.updateCustomer(id, profileRequest);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cập nhật thông tin khách hàng \"" + updated.fullName() + "\" (Mã: #" + id + ") thành công!");
            return "redirect:/admin/customers";
        } catch (BusinessException ex) {
            CustomerResponse customer = customerService.getCustomerById(id);
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("customer", customer);
            model.addAttribute("customerId", id);
            model.addAttribute("activeTab", "customers");
            return "admin/customer/edit";
        }
    }
}

