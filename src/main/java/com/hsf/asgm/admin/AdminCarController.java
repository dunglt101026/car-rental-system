package com.hsf.asgm.admin;

import com.hsf.asgm.car.*;
import com.hsf.asgm.common.exception.BusinessException;
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
@RequestMapping("/admin/cars")
@RequiredArgsConstructor
@Slf4j
public class AdminCarController {

    private final CarService carService;

    @GetMapping
    public String listCars(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "producerId", required = false) Integer producerId,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Model model
    ) {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;

        Page<CarResponse> carsPage = carService.getAdminCars(keyword, producerId, status, page, size);

        model.addAttribute("carsPage", carsPage);
        model.addAttribute("cars", carsPage.getContent());
        model.addAttribute("producers", carService.getAllProducers());
        model.addAttribute("statuses", CarStatus.values());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedProducerId", producerId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", carsPage.getTotalPages());
        model.addAttribute("totalElements", carsPage.getTotalElements());
        model.addAttribute("activeTab", "cars");

        return "admin/car/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("carForm")) {
            model.addAttribute("carForm", CarRequest.empty());
        }
        model.addAttribute("producers", carService.getAllProducers());
        model.addAttribute("statuses", CarStatus.values());
        model.addAttribute("isEdit", false);
        model.addAttribute("activeTab", "cars");
        return "admin/car/form";
    }

    @PostMapping("/new")
    public String handleCreateCar(
            @Valid @ModelAttribute("carForm") CarRequest carForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("producers", carService.getAllProducers());
            model.addAttribute("statuses", CarStatus.values());
            model.addAttribute("isEdit", false);
            model.addAttribute("activeTab", "cars");
            return "admin/car/form";
        }

        try {
            CarResponse created = carService.createCar(carForm);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Thêm xe mới thành công: " + created.carName() + " (Mã: #" + created.carId() + ")");
            return "redirect:/admin/cars";
        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("producers", carService.getAllProducers());
            model.addAttribute("statuses", CarStatus.values());
            model.addAttribute("isEdit", false);
            model.addAttribute("activeTab", "cars");
            return "admin/car/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        if (!model.containsAttribute("carForm")) {
            CarRequest form = carService.getCarFormById(id);
            model.addAttribute("carForm", form);
        }
        model.addAttribute("carId", id);
        model.addAttribute("producers", carService.getAllProducers());
        model.addAttribute("statuses", CarStatus.values());
        model.addAttribute("isEdit", true);
        model.addAttribute("activeTab", "cars");
        return "admin/car/form";
    }

    @PostMapping("/edit/{id}")
    public String handleUpdateCar(
            @PathVariable("id") Integer id,
            @Valid @ModelAttribute("carForm") CarRequest carForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("carId", id);
            model.addAttribute("producers", carService.getAllProducers());
            model.addAttribute("statuses", CarStatus.values());
            model.addAttribute("isEdit", true);
            model.addAttribute("activeTab", "cars");
            return "admin/car/form";
        }

        try {
            CarResponse updated = carService.updateCar(id, carForm);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cập nhật thông tin xe thành công: " + updated.carName() + " (Mã: #" + updated.carId() + ")");
            return "redirect:/admin/cars";
        } catch (BusinessException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("carId", id);
            model.addAttribute("producers", carService.getAllProducers());
            model.addAttribute("statuses", CarStatus.values());
            model.addAttribute("isEdit", true);
            model.addAttribute("activeTab", "cars");
            return "admin/car/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String handleDeleteCar(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        CarDeleteResult result = carService.deleteCar(id);
        redirectAttributes.addFlashAttribute("successMessage", result.getDisplayMessage());
        return "redirect:/admin/cars";
    }
}

