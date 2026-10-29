package com.hsf.asgm.admin;

import com.hsf.asgm.common.exception.BusinessException;
import com.hsf.asgm.customer.CustomerProfileRequest;
import com.hsf.asgm.customer.CustomerResponse;
import com.hsf.asgm.customer.CustomerService;
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

