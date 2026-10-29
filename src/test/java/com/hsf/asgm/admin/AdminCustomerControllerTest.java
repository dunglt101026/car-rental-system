package com.hsf.asgm.admin;

import com.hsf.asgm.auth.AccountResponse;
import com.hsf.asgm.common.config.AuthInterceptor;
import com.hsf.asgm.customer.CustomerProfileRequest;
import com.hsf.asgm.customer.CustomerResponse;
import com.hsf.asgm.customer.CustomerService;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminCustomerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private AdminCustomerController adminCustomerController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminCustomerController)
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
    @DisplayName("GET /admin/customers - Chưa đăng nhập -> Chặn và chuyển hướng sang /login")
    void testListCustomers_Unauthenticated_ShouldRedirectLogin() throws Exception {
        mockMvc.perform(get("/admin/customers"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=unauthorized"));
    }

    @Test
    @DisplayName("GET /admin/customers - Quyền Customer -> Chặn và chuyển hướng sang /403")
    void testListCustomers_ForbiddenRole_ShouldRedirect403() throws Exception {
        mockMvc.perform(get("/admin/customers").session(createCustomerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/403"));
    }

    @Test
    @DisplayName("GET /admin/customers - Quyền Admin -> Hiển thị danh sách khách hàng")
    void testListCustomers_AdminRole_ShouldReturnListView() throws Exception {
        CustomerResponse customer = new CustomerResponse(
                1, "Nguyễn Văn A", "0912345678", LocalDate.of(1990, 5, 10),
                "012345678901", "B2-111111", LocalDate.of(2018, 5, 10),
                1, "customer01", "customer01@fucar.com"
        );
        Page<CustomerResponse> page = new PageImpl<>(List.of(customer));
        when(customerService.getAdminCustomers(any(), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/admin/customers").session(createAdminSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/customer/list"))
                .andExpect(model().attributeExists("customersPage", "customers", "totalElements"));
    }

    @Test
    @DisplayName("GET /admin/customers/edit/{id} - Quyền Admin -> Hiển thị form chỉnh sửa hồ sơ khách hàng")
    void testShowEditForm_AdminRole_ShouldReturnEditView() throws Exception {
        CustomerResponse customer = new CustomerResponse(
                1, "Nguyễn Văn A", "0912345678", LocalDate.of(1990, 5, 10),
                "012345678901", "B2-111111", LocalDate.of(2018, 5, 10),
                1, "customer01", "customer01@fucar.com"
        );
        CustomerProfileRequest form = new CustomerProfileRequest(
                "Nguyễn Văn A", "0912345678", LocalDate.of(1990, 5, 10),
                "012345678901", "B2-111111", LocalDate.of(2018, 5, 10)
        );
        when(customerService.getCustomerById(1)).thenReturn(customer);
        when(customerService.getCustomerFormById(1)).thenReturn(form);

        mockMvc.perform(get("/admin/customers/edit/1").session(createAdminSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/customer/edit"))
                .andExpect(model().attributeExists("customer", "profileRequest", "customerId"));
    }

    @Test
    @DisplayName("POST /admin/customers/edit/{id} - Dữ liệu trống -> Trả về form với lỗi Bean Validation")
    void testUpdateCustomer_ValidationError_ShouldReturnEditFormWithErrors() throws Exception {
        CustomerResponse customer = new CustomerResponse(
                1, "Nguyễn Văn A", "0912345678", LocalDate.of(1990, 5, 10),
                "012345678901", "B2-111111", LocalDate.of(2018, 5, 10),
                1, "customer01", "customer01@fucar.com"
        );
        when(customerService.getCustomerById(1)).thenReturn(customer);

        mockMvc.perform(post("/admin/customers/edit/1")
                        .session(createAdminSession())
                        .param("fullName", "")
                        .param("mobile", "")
                        .param("birthday", "")
                        .param("identityCard", "")
                        .param("licenceNumber", "")
                        .param("licenceDate", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/customer/edit"))
                .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("POST /admin/customers/edit/{id} - Dữ liệu hợp lệ -> Cập nhật thành công và redirect về danh sách")
    void testUpdateCustomer_Success_ShouldRedirectWithFlashMessage() throws Exception {
        CustomerResponse updated = new CustomerResponse(
                1, "Nguyễn Văn A Updated", "0912345678", LocalDate.of(1990, 5, 10),
                "012345678901", "B2-111111", LocalDate.of(2018, 5, 10),
                1, "customer01", "customer01@fucar.com"
        );
        when(customerService.updateCustomer(eq(1), any(CustomerProfileRequest.class))).thenReturn(updated);

        mockMvc.perform(post("/admin/customers/edit/1")
                        .session(createAdminSession())
                        .param("fullName", "Nguyễn Văn A Updated")
                        .param("mobile", "0912345678")
                        .param("birthday", "1990-05-10")
                        .param("identityCard", "012345678901")
                        .param("licenceNumber", "B2-111111")
                        .param("licenceDate", "2018-05-10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/customers"))
                .andExpect(flash().attributeExists("successMessage"));
    }
}

