package com.hsf.asgm.common.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.http.HttpMethod;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private Model model;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        model = new ConcurrentModel();
    }

    @Test
    @DisplayName("handleResourceNotFound - Trả về view error/404 và gán errorMessage")
    void testHandleResourceNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Không tìm thấy xe #999");
        String view = exceptionHandler.handleResourceNotFound(ex, model);

        assertThat(view).isEqualTo("error/404");
        assertThat(model.getAttribute("errorMessage")).isEqualTo("Không tìm thấy xe #999");
    }

    @Test
    @DisplayName("handleNoResourceFound - Trả về view error/404")
    void testHandleNoResourceFound() {
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/unknown-path", "Resource not found");
        String view = exceptionHandler.handleNoResourceFound(ex, model);

        assertThat(view).isEqualTo("error/404");
        assertThat(model.getAttribute("errorMessage")).isEqualTo("Đường dẫn không tồn tại trên hệ thống.");
    }

    @Test
    @DisplayName("handleMethodArgumentTypeMismatch - Trả về view error/404 khi tham số sai kiểu")
    void testHandleMethodArgumentTypeMismatch() throws NoSuchMethodException {
        var method = this.getClass().getDeclaredMethod("setUp");
        MethodParameter param = new MethodParameter(method, -1);
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException("abc", Integer.class, "id", param, null);

        String view = exceptionHandler.handleMethodArgumentTypeMismatch(ex, model);

        assertThat(view).isEqualTo("error/404");
        assertThat(model.getAttribute("errorMessage")).isEqualTo("Tham số yêu cầu không đúng định dạng hoặc tài nguyên không tồn tại.");
    }

    @Test
    @DisplayName("handleMissingParams - Trả về view error/400 khi thiếu tham số bắt buộc")
    void testHandleMissingParams() {
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("carId", "Integer");
        String view = exceptionHandler.handleMissingParams(ex, model);

        assertThat(view).isEqualTo("error/400");
        assertThat(model.getAttribute("errorMessage")).isEqualTo("Thiếu tham số bắt buộc: carId");
    }

    @Test
    @DisplayName("handleBusinessException - Trả về view error/400")
    void testHandleBusinessException() {
        BusinessException ex = new BusinessException("Xe đã được thuê trong thời gian này");
        String view = exceptionHandler.handleBusinessException(ex, model);

        assertThat(view).isEqualTo("error/400");
        assertThat(model.getAttribute("errorMessage")).isEqualTo("Xe đã được thuê trong thời gian này");
    }

    @Test
    @DisplayName("handleGeneralException - Trả về view error/500")
    void testHandleGeneralException() {
        RuntimeException ex = new RuntimeException("Lỗi kết nối cơ sở dữ liệu");
        String view = exceptionHandler.handleGeneralException(ex, model);

        assertThat(view).isEqualTo("error/500");
        assertThat(model.getAttribute("errorMessage")).isEqualTo("Đã có sự cố bất ngờ xảy ra phía máy chủ. Vui lòng thử lại sau.");
    }
}
