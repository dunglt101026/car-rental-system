package com.hsf.asgm.admin;

import com.hsf.asgm.common.exception.BusinessException;
import com.hsf.asgm.rental.RentalResponse;
import com.hsf.asgm.rental.RentalService;
import com.hsf.asgm.rental.RentalStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/rentals")
@RequiredArgsConstructor
@Slf4j
public class AdminRentalController {

    private final RentalService rentalService;

    @GetMapping
    public String listRentals(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Model model
    ) {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;

        Page<RentalResponse> rentalsPage = rentalService.getAdminRentals(keyword, status, page, size);

        model.addAttribute("rentalsPage", rentalsPage);
        model.addAttribute("rentals", rentalsPage.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", RentalStatus.values());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", rentalsPage.getTotalPages());
        model.addAttribute("totalElements", rentalsPage.getTotalElements());
        model.addAttribute("activeTab", "rentals");

        return "admin/rental/list";
    }

    @GetMapping("/{id}")
    public String viewRentalDetail(@PathVariable("id") Integer id, Model model) {
        RentalResponse rental = rentalService.getRentalById(id);
        model.addAttribute("rental", rental);
        model.addAttribute("activeTab", "rentals");

        return "admin/rental/detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable("id") Integer id,
            @RequestParam("status") String newStatus,
            @RequestParam(value = "redirectToList", defaultValue = "false") boolean redirectToList,
            RedirectAttributes redirectAttributes
    ) {
        try {
            RentalResponse updated = rentalService.updateRentalStatus(id, newStatus);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cập nhật đơn thuê #" + id + " sang trạng thái \"" + updated.status() + "\" thành công. Trạng thái xe liên kết đã được đồng bộ!");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        if (redirectToList) {
            return "redirect:/admin/rentals";
        }
        return "redirect:/admin/rentals/" + id;
    }
}

