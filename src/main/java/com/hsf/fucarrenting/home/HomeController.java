package com.hsf.fucarrenting.home;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class HomeController {

    @GetMapping({"/", "/home"})
    public String home() {
        return "home";
    }

    @GetMapping("/403")
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String accessDenied() {
        return "error/403";
    }
}


