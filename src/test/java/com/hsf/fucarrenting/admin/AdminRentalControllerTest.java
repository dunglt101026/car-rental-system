package com.hsf.fucarrenting.admin;

import com.hsf.fucarrenting.rental.controller.AdminRentalController;
import com.hsf.fucarrenting.auth.dto.AccountResponse;
import com.hsf.fucarrenting.common.config.AuthInterceptor;
import com.hsf.fucarrenting.rental.dto.RentalResponse;
import com.hsf.fucarrenting.rental.service.RentalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminRentalControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RentalService rentalService;

    @InjectMocks
    private AdminRentalController adminRentalController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminRentalController)
                .addInterceptors(new AuthInterceptor())
                .build();
    }

    private MockHttpSession createAdminSession() {
        MockHttpSession session = new MockHttpSession();
        AccountResponse adminUser = new AccountResponse(1, "admin", "admin@fucar.com", "Admin", null, "Administrator");
        session.setAttribute(AuthInterceptor.SESSION_USER, adminUser);
        return session;
    }

    private MockHttpSession createCustomerSession() {
        MockHttpSession session = new MockHttpSession();
        AccountResponse customerUser = new AccountResponse(2, "customer", "customer@fucar.com", "Customer", 5, "Nguyen Van A");
        session.setAttribute(AuthInterceptor.SESSION_USER, customerUser);
        return session;
    }

    @Test
    @DisplayName("GET /admin/rentals - Chưa đăng nhập -> Chuyển hướng /login")
    void testListRentals_Unauthenticated_ShouldRedirectLogin() throws Exception {
        mockMvc.perform(get("/admin/rentals"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=unauthorized"));
    }

    @Test
    @DisplayName("GET /admin/rentals - Quyền Customer -> Chuyển hướng /403")
    void testListRentals_CustomerRole_ShouldRedirect403() throws Exception {
        mockMvc.perform(get("/admin/rentals").session(createCustomerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/403"));
    }

    @Test
    @DisplayName("GET /admin/rentals - Quyền Admin -> Hiển thị danh sách đơn thuê")
    void testListRentals_AdminRole_ShouldReturnListView() throws Exception {
        RentalResponse rental = new RentalResponse(
                10, 1, "Nguyễn Văn A", "0912345678", "a@test.com",
                1, "Toyota Corolla", "Toyota",
                LocalDate.now(), LocalDate.now().plusDays(3),
                BigDecimal.valueOf(3000000), "Active", null, null
        );
        Page<RentalResponse> page = new PageImpl<>(List.of(rental));
        when(rentalService.getAdminRentals(any(), any(), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/admin/rentals").session(createAdminSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/rental/list"))
                .andExpect(model().attributeExists("rentalsPage", "rentals", "statuses"));
    }

    @Test
    @DisplayName("GET /admin/rentals/{id} - Quyền Admin -> Hiển thị chi tiết đơn thuê")
    void testViewRentalDetail_AdminRole_ShouldReturnDetailView() throws Exception {
        RentalResponse rental = new RentalResponse(
                10, 1, "Nguyễn Văn A", "0912345678", "a@test.com",
                1, "Toyota Corolla", "Toyota",
                LocalDate.now(), LocalDate.now().plusDays(3),
                BigDecimal.valueOf(3000000), "Active", 5, "Rất tốt"
        );
        when(rentalService.getRentalById(10)).thenReturn(rental);

        mockMvc.perform(get("/admin/rentals/10").session(createAdminSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/rental/detail"))
                .andExpect(model().attributeExists("rental"));
    }

    @Test
    @DisplayName("POST /admin/rentals/{id}/status - Cập nhật trạng thái thành công -> Redirect kèm Flash Message")
    void testUpdateStatus_Success_ShouldRedirect() throws Exception {
        RentalResponse updated = new RentalResponse(
                10, 1, "Nguyễn Văn A", "0912345678", "a@test.com",
                1, "Toyota Corolla", "Toyota",
                LocalDate.now(), LocalDate.now().plusDays(3),
                BigDecimal.valueOf(3000000), "Completed", null, null
        );
        when(rentalService.updateRentalStatus(10, "Completed")).thenReturn(updated);

        mockMvc.perform(post("/admin/rentals/10/status")
                        .session(createAdminSession())
                        .param("status", "Completed")
                        .param("redirectToList", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/rentals"))
                .andExpect(flash().attributeExists("successMessage"));
    }
}

