package com.hsf.fucarrenting.admin;

import com.hsf.fucarrenting.auth.dto.AccountResponse;
import com.hsf.fucarrenting.car.controller.AdminCarController;
import com.hsf.fucarrenting.car.dto.CarDeleteResult;
import com.hsf.fucarrenting.car.dto.CarRequest;
import com.hsf.fucarrenting.car.dto.CarResponse;
import com.hsf.fucarrenting.car.service.CarServiceImpl;
import com.hsf.fucarrenting.common.config.AuthInterceptor;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminCarControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CarServiceImpl carService;

    @InjectMocks
    private AdminCarController adminCarController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminCarController)
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
    @DisplayName("GET /admin/cars - Chưa đăng nhập -> Chặn và chuyển hướng sang /login")
    void testListCars_Unauthenticated_ShouldRedirectLogin() throws Exception {
        mockMvc.perform(get("/admin/cars"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=unauthorized"));
    }

    @Test
    @DisplayName("GET /admin/cars - Đăng nhập quyền Customer -> Chặn và chuyển hướng sang /403")
    void testListCars_ForbiddenRole_ShouldRedirect403() throws Exception {
        mockMvc.perform(get("/admin/cars").session(createCustomerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/403"));
    }

    @Test
    @DisplayName("GET /admin/cars - Đăng nhập quyền Admin -> Hiển thị trang quản lý xe")
    void testListCars_AdminRole_ShouldReturnListView() throws Exception {
        CarResponse car = new CarResponse(
                1, "Toyota Corolla Cross", 2023, "Trắng", 5, "Mô tả",
                LocalDate.now(), 1, "Toyota", BigDecimal.valueOf(900000), "Available"
        );
        Page<CarResponse> page = new PageImpl<>(List.of(car));
        when(carService.getAdminCars(any(), any(), any(), anyInt(), anyInt())).thenReturn(page);
        when(carService.getAllProducers()).thenReturn(List.of());

        mockMvc.perform(get("/admin/cars").session(createAdminSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/car/list"))
                .andExpect(model().attributeExists("carsPage", "cars", "producers", "statuses"));
    }

    @Test
    @DisplayName("GET /admin/cars/new - Đăng nhập quyền Admin -> Hiển thị form thêm mới xe")
    void testShowCreateForm_AdminRole_ShouldReturnFormView() throws Exception {
        when(carService.getAllProducers()).thenReturn(List.of());

        mockMvc.perform(get("/admin/cars/new").session(createAdminSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/car/form"))
                .andExpect(model().attribute("isEdit", false))
                .andExpect(model().attributeExists("carForm", "producers", "statuses"));
    }

    @Test
    @DisplayName("POST /admin/cars/new - Dữ liệu trống -> Trả về form với lỗi Bean Validation")
    void testCreateCar_ValidationError_ShouldReturnFormWithErrors() throws Exception {
        when(carService.getAllProducers()).thenReturn(List.of());

        mockMvc.perform(post("/admin/cars/new")
                        .session(createAdminSession())
                        .param("carName", "")
                        .param("carModelYear", "")
                        .param("color", "")
                        .param("capacity", "")
                        .param("description", "")
                        .param("importDate", "")
                        .param("rentPrice", "")
                        .param("status", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/car/form"))
                .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("POST /admin/cars/new - Giá có phần thập phân -> Báo lỗi tại ô giá, không tạo xe")
    void testCreateCar_DecimalPrice_ShouldReturnFieldError() throws Exception {
        when(carService.getAllProducers()).thenReturn(List.of());

        mockMvc.perform(post("/admin/cars/new")
                        .session(createAdminSession())
                        .param("carName", "Mazda CX-5")
                        .param("producerId", "1")
                        .param("carModelYear", "2023")
                        .param("color", "Đỏ")
                        .param("capacity", "5")
                        .param("rentPrice", "80.5")
                        .param("importDate", "2023-05-10")
                        .param("status", "Available")
                        .param("description", "SUV thể thao hiện đại"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/car/form"))
                .andExpect(model().attributeHasFieldErrors("carForm", "rentPrice"));

        verify(carService, never()).createCar(any());
    }

    @Test
    @DisplayName("POST /admin/cars/new - Dữ liệu hợp lệ -> Tạo xe và redirect về /admin/cars với flash message")
    void testCreateCar_Success_ShouldRedirect() throws Exception {
        CarResponse created = new CarResponse(
                10, "Mazda CX-5", 2023, "Đỏ", 5, "SUV thể thao",
                LocalDate.now(), 1, "Mazda", BigDecimal.valueOf(1100000), "Available"
        );
        when(carService.createCar(any(CarRequest.class))).thenReturn(created);

        mockMvc.perform(post("/admin/cars/new")
                        .session(createAdminSession())
                        .param("carName", "Mazda CX-5")
                        .param("producerId", "1")
                        .param("carModelYear", "2023")
                        .param("color", "Đỏ")
                        .param("capacity", "5")
                        .param("rentPrice", "1100000")
                        .param("importDate", "2023-05-10")
                        .param("status", "Available")
                        .param("description", "SUV thể thao hiện đại"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/cars"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("POST /admin/cars/delete/{id} - Xóa xe -> Redirect về /admin/cars với flash message thông báo kết quả")
    void testDeleteCar_ShouldRedirectWithFlashMessage() throws Exception {
        CarDeleteResult deleteResult = new CarDeleteResult(true, "Mazda CX-5", 10);
        when(carService.deleteCar(10)).thenReturn(deleteResult);

        mockMvc.perform(post("/admin/cars/delete/10")
                        .session(createAdminSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/cars"))
                .andExpect(flash().attributeExists("successMessage"));
    }
}
