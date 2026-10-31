package com.hsf.fucarrenting.car.controller;

import com.hsf.fucarrenting.car.dto.CarProducerResponse;
import com.hsf.fucarrenting.car.dto.CarResponse;
import com.hsf.fucarrenting.car.service.CarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/cars")
@RequiredArgsConstructor
@Slf4j
public class CarController {

    private final CarService carService;

    @GetMapping
    public String listCars(
            @RequestParam(value = "producerId", required = false) Integer producerId,
            @RequestParam(value = "capacity", required = false) Integer capacity,
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model
    ) {
        log.debug("Fetching available cars with filters - producer: {}, capacity: {}, minPrice: {}, maxPrice: {}, kw: {}",
                producerId, capacity, minPrice, maxPrice, keyword);

        List<CarResponse> cars = carService.getAvailableCars(producerId, capacity, minPrice, maxPrice, keyword);
        List<CarProducerResponse> producers = carService.getAllProducers();

        model.addAttribute("cars", cars);
        model.addAttribute("producers", producers);
        model.addAttribute("selectedProducerId", producerId);
        model.addAttribute("selectedCapacity", capacity);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("keyword", keyword);

        return "car/list";
    }

    @GetMapping("/{id}")
    public String carDetail(@PathVariable("id") Integer id, Model model) {
        CarResponse car = carService.getCarById(id);
        model.addAttribute("car", car);
        return "car/detail";
    }
}

