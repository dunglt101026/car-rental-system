package com.hsf.fucarrenting.admin;

import com.hsf.fucarrenting.admin.controller.AdminReportController;
import com.hsf.fucarrenting.auth.AccountResponse;
import com.hsf.fucarrenting.common.config.AuthInterceptor;
import com.hsf.fucarrenting.report.RentalReportResponse;
import com.hsf.fucarrenting.report.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminReportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReportService reportService;

    @InjectMocks
    private AdminReportController adminReportController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminReportController)
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
    @DisplayName("GET /admin/reports - Chưa đăng nhập -> Chuyển hướng /login")
    void testShowReport_Unauthenticated_ShouldRedirectLogin() throws Exception {
        mockMvc.perform(get("/admin/reports"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=unauthorized"));
    }

    @Test
    @DisplayName("GET /admin/reports - Quyền Customer -> Chuyển hướng /403")
    void testShowReport_CustomerRole_ShouldRedirect403() throws Exception {
        mockMvc.perform(get("/admin/reports").session(createCustomerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/403"));
    }

    @Test
    @DisplayName("GET /admin/reports - Quyền Admin -> Hiển thị trang báo cáo doanh thu")
    void testShowReport_AdminRole_ShouldReturnReportView() throws Exception {
        RentalReportResponse mockReport = new RentalReportResponse(
                LocalDate.now().minusDays(30),
                LocalDate.now(),
                "date",
                0,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0,
                0,
                0,
                List.of()
        );
        when(reportService.generateReport(any(), any(), anyString())).thenReturn(mockReport);

        mockMvc.perform(get("/admin/reports").session(createAdminSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/report/index"))
                .andExpect(model().attributeExists("report", "startDate", "endDate", "sortBy", "activeTab"));
    }
}

