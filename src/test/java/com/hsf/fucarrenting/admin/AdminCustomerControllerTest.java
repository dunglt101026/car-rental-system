package com.hsf.fucarrenting.admin;

import com.hsf.fucarrenting.customer.controller.AdminCustomerController;
import com.hsf.fucarrenting.auth.dto.AccountResponse;
import com.hsf.fucarrenting.auth.service.AuthService;
import com.hsf.fucarrenting.auth.dto.RegisterRequest;
import com.hsf.fucarrenting.common.exception.BusinessException;
import com.hsf.fucarrenting.common.config.AuthInterceptor;
import com.hsf.fucarrenting.customer.dto.CustomerProfileRequest;
import com.hsf.fucarrenting.customer.dto.CustomerResponse;
import com.hsf.fucarrenting.customer.service.CustomerService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminCustomerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CustomerService customerService;

    @Mock
    private AuthService authService;

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

    @Test
    @DisplayName("GET /admin/customers/new - Quyền Admin -> Hiển thị form thêm khách hàng")
    void testShowCreateForm_AdminRole_ShouldReturnCreateView() throws Exception {
        mockMvc.perform(get("/admin/customers/new").session(createAdminSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/customer/create"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    @DisplayName("POST /admin/customers/new - Dữ liệu hợp lệ -> Tạo khách hàng và chuyển về danh sách")
    void testCreateCustomer_ValidData_ShouldRedirectList() throws Exception {
        mockMvc.perform(post("/admin/customers/new")
                        .session(createAdminSession())
                        .param("accountName", "newcustomer")
                        .param("email", "newcustomer@fucar.com")
                        .param("password", "secret123")
                        .param("fullName", "Trần Thị B")
                        .param("mobile", "0987654321")
                        .param("birthday", "1995-03-15")
                        .param("identityCard", "098765432109")
                        .param("licenceNumber", "B2-222222")
                        .param("licenceDate", "2019-06-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/customers"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("POST /admin/customers/new - Thiếu dữ liệu -> Hiển thị lại form, không tạo khách hàng")
    void testCreateCustomer_InvalidData_ShouldReturnForm() throws Exception {
        mockMvc.perform(post("/admin/customers/new")
                        .session(createAdminSession())
                        .param("accountName", "")
                        .param("email", "invalid"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/customer/create"))
                .andExpect(model().attributeHasFieldErrors("registerRequest", "accountName", "email"));

        verify(authService, never()).register(any());
    }

    @Test
    @DisplayName("POST /admin/customers/delete/{id} - Khách chưa có đơn thuê -> Xóa thành công")
    void testDeleteCustomer_Success_ShouldRedirectWithSuccess() throws Exception {
        when(customerService.deleteCustomer(7)).thenReturn("Trần Thị B");

        mockMvc.perform(post("/admin/customers/delete/7").session(createAdminSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/customers"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("POST /admin/customers/delete/{id} - Khách đã có đơn thuê -> Báo lỗi")
    void testDeleteCustomer_HasRentals_ShouldRedirectWithError() throws Exception {
        when(customerService.deleteCustomer(1)).thenThrow(new BusinessException("Không thể xóa khách hàng"));

        mockMvc.perform(post("/admin/customers/delete/1").session(createAdminSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/customers"))
                .andExpect(flash().attribute("errorMessage", "Không thể xóa khách hàng"));
    }
}
