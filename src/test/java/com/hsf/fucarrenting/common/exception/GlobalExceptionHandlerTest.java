package com.hsf.fucarrenting.common.exception;

import com.hsf.fucarrenting.auth.AccountResponse;
import com.hsf.fucarrenting.common.config.AuthInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private Model model;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        model = new ConcurrentModel();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("ResourceNotFoundException - Trả về view error/404, HTTP 404")
    void testHandleResourceNotFound() {
        String view = exceptionHandler.handleAppException(
                new ResourceNotFoundException("Không tìm thấy xe #999"), model, request, response);

        assertThat(view).isEqualTo("error/404");
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(model.getAttribute("errorMessage")).isEqualTo("Không tìm thấy xe #999");
    }

    @Test
    @DisplayName("BusinessException - Trả về view error/400, HTTP 400")
    void testHandleBusinessException() {
        String view = exceptionHandler.handleAppException(
                new BusinessException("Xe đã được thuê trong thời gian này"), model, request, response);

        assertThat(view).isEqualTo("error/400");
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(model.getAttribute("errorMessage")).isEqualTo("Xe đã được thuê trong thời gian này");
    }

    @Test
    @DisplayName("BusinessException và ResourceNotFoundException cùng kế thừa AppException")
    void testExceptionHierarchy() {
        assertThat(new BusinessException("x")).isInstanceOf(AppException.class);
        assertThat(new ResourceNotFoundException("x")).isInstanceOf(AppException.class);
    }

    @Test
    @DisplayName("handleNoResourceFound - Trả về view error/404")
    void testHandleNoResourceFound() {
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/unknown-path", "Resource not found");
        String view = exceptionHandler.handleNoResourceFound(ex, model, request, response);

        assertThat(view).isEqualTo("error/404");
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(model.getAttribute("errorMessage")).isEqualTo("Đường dẫn không tồn tại trên hệ thống.");
    }

    @Test
    @DisplayName("handleMethodArgumentTypeMismatch - Trả về view error/404 khi tham số sai kiểu")
    void testHandleMethodArgumentTypeMismatch() throws NoSuchMethodException {
        var method = this.getClass().getDeclaredMethod("setUp");
        MethodParameter param = new MethodParameter(method, -1);
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException("abc", Integer.class, "id", param, null);

        String view = exceptionHandler.handleMethodArgumentTypeMismatch(ex, model, request, response);

        assertThat(view).isEqualTo("error/404");
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(model.getAttribute("errorMessage")).isEqualTo("Tham số yêu cầu không đúng định dạng hoặc tài nguyên không tồn tại.");
    }

    @Test
    @DisplayName("handleMissingParams - Trả về view error/400 khi thiếu tham số bắt buộc")
    void testHandleMissingParams() {
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("carId", "Integer");
        String view = exceptionHandler.handleMissingParams(ex, model, request, response);

        assertThat(view).isEqualTo("error/400");
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(model.getAttribute("errorMessage")).isEqualTo("Thiếu tham số bắt buộc: carId");
    }

    @Test
    @DisplayName("handleGeneralException - Trả về view error/500, không lộ chi tiết lỗi")
    void testHandleGeneralException() {
        String view = exceptionHandler.handleGeneralException(
                new RuntimeException("Lỗi kết nối cơ sở dữ liệu"), model, request, response);

        assertThat(view).isEqualTo("error/500");
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(model.getAttribute("errorMessage")).isEqualTo("Đã có sự cố bất ngờ xảy ra phía máy chủ. Vui lòng thử lại sau.");
    }

    @Test
    @DisplayName("Trang lỗi nhận currentUser từ session để navbar hiển thị đúng người dùng")
    void testErrorPageIncludesCurrentUser() {
        AccountResponse admin = new AccountResponse(1, "admin", "admin@fucar.com", "Admin", null, "Administrator");
        request.getSession(true).setAttribute(AuthInterceptor.SESSION_USER, admin);

        exceptionHandler.handleAppException(new ResourceNotFoundException("x"), model, request, response);

        assertThat(model.getAttribute("currentUser")).isEqualTo(admin);
    }
}
