package com.hsf.fucarrenting.home.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class HomeControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new HomeController()).build();
    }

    @Test
    @DisplayName("GET / -> Trang chủ")
    void testRoot_ShouldReturnHome() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));
    }

    @Test
    @DisplayName("GET /admin -> Chuyển về /admin/cars")
    void testAdminRoot_ShouldRedirectToCars() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/cars"));
    }

    @Test
    @DisplayName("GET /403 -> Trang 403 với HTTP 403")
    void testAccessDenied_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/403"))
                .andExpect(status().isForbidden())
                .andExpect(view().name("error/403"));
    }
}
