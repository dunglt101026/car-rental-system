package com.hsf.fucarrenting.review.controller;

import com.hsf.fucarrenting.auth.dto.AccountResponse;
import com.hsf.fucarrenting.common.config.AuthInterceptor;
import com.hsf.fucarrenting.common.exception.BusinessException;
import com.hsf.fucarrenting.customer.dto.CustomerResponse;
import com.hsf.fucarrenting.customer.service.CustomerService;
import com.hsf.fucarrenting.rental.dto.RentalResponse;
import com.hsf.fucarrenting.review.service.ReviewService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReviewService reviewService;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private ReviewController reviewController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController)
                .addInterceptors(new AuthInterceptor())
                .build();
    }

    private MockHttpSession createCustomerSession() {
        MockHttpSession session = new MockHttpSession();
        AccountResponse customerUser = new AccountResponse(2, "customer4", "customer4@gmail.com", "Customer", 5, "Phạm Thị Dung");
        session.setAttribute(AuthInterceptor.SESSION_USER, customerUser);
        return session;
    }

    private CustomerResponse customer() {
        return new CustomerResponse(5, "Phạm Thị Dung", "0911000004", LocalDate.of(1992, 4, 4),
                "001092000004", "B2-000004", LocalDate.of(2016, 4, 4), 2, "customer4", "customer4@gmail.com");
    }

    private RentalResponse completedRental() {
        return new RentalResponse(3, 5, "Phạm Thị Dung", "0911000004", "customer4@gmail.com", 8,
                "Mitsubishi Xpander", "Mitsubishi", LocalDate.of(2026, 10, 11), LocalDate.of(2026, 10, 14),
                BigDecimal.valueOf(165), "Completed", null, null);
    }

    @Test
    @DisplayName("GET /customer/reviews/new - Đơn Completed chưa đánh giá -> Mở trang đánh giá")
    void testShowReviewForm_Reviewable_ShouldShowPage() throws Exception {
        when(customerService.getCustomerByAccountId(2)).thenReturn(customer());
        when(reviewService.getReviewableRental(5, 3)).thenReturn(completedRental());

        mockMvc.perform(get("/customer/reviews/new").param("carRentId", "3").session(createCustomerSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("review/form"))
                .andExpect(model().attributeExists("rental", "reviewRequest"));
    }

    @Test
    @DisplayName("GET /customer/reviews/new - Đơn của người khác -> Về lịch sử thuê kèm thông báo")
    void testShowReviewForm_NotOwner_ShouldRedirect() throws Exception {
        when(customerService.getCustomerByAccountId(2)).thenReturn(customer());
        when(reviewService.getReviewableRental(5, 1))
                .thenThrow(new BusinessException("Bạn không có quyền đánh giá đơn thuê xe này."));

        mockMvc.perform(get("/customer/reviews/new").param("carRentId", "1").session(createCustomerSession()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customer/rentals"))
                .andExpect(flash().attribute("errorMessage", "Bạn không có quyền đánh giá đơn thuê xe này."));
    }

    @Test
    @DisplayName("POST /customer/reviews - Nhận xét trống -> Ở lại trang đánh giá, báo lỗi tại ô")
    void testCreateReview_BlankComment_ShouldStayOnForm() throws Exception {
        when(customerService.getCustomerByAccountId(2)).thenReturn(customer());
        when(reviewService.getReviewableRental(5, 3)).thenReturn(completedRental());

        mockMvc.perform(post("/customer/reviews").session(createCustomerSession())
                        .param("carRentId", "3")
                        .param("reviewStar", "5")
                        .param("comment", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("review/form"))
                .andExpect(model().attributeHasFieldErrors("reviewRequest", "comment"));

        verify(reviewService, never()).createReview(any(), any());
    }

    @Test
    @DisplayName("POST /customer/reviews - Hợp lệ -> Lưu và về lịch sử thuê")
    void testCreateReview_Valid_ShouldRedirect() throws Exception {
        when(customerService.getCustomerByAccountId(2)).thenReturn(customer());

        mockMvc.perform(post("/customer/reviews").session(createCustomerSession())
                        .param("carRentId", "3")
                        .param("reviewStar", "5")
                        .param("comment", "Xe tốt"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customer/rentals"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(reviewService).createReview(eq(5), any());
    }
}
