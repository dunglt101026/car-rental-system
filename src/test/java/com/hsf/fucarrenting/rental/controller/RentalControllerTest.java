package com.hsf.fucarrenting.rental.controller;

import com.hsf.fucarrenting.auth.dto.AccountResponse;
import com.hsf.fucarrenting.car.service.CarService;
import com.hsf.fucarrenting.common.config.AuthInterceptor;
import com.hsf.fucarrenting.customer.dto.CustomerResponse;
import com.hsf.fucarrenting.customer.service.CustomerService;
import com.hsf.fucarrenting.rental.dto.RentalQuoteResponse;
import com.hsf.fucarrenting.rental.service.RentalService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RentalControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RentalService rentalService;

    @Mock
    private CarService carService;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private RentalController rentalController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(rentalController)
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

    @Test
    @DisplayName("GET /customer/rentals/book - Không có carId -> Vẫn mở trang chọn xe")
    void testShowBookingForm_WithoutCarId_ShouldShowPage() throws Exception {
        when(customerService.getCustomerByAccountId(2)).thenReturn(customer());
        when(carService.getAvailableCars(any(), any(), any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/customer/rentals/book").session(createCustomerSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("rental/book"))
                .andExpect(model().attributeExists("rentalRequest", "availableCars"));

        verify(carService, never()).getCarById(any());
    }

    @Test
    @DisplayName("POST /customer/rentals/book action=quote -> Chỉ tính chi phí, không tạo đơn")
    void testQuote_ShouldShowQuoteWithoutBooking() throws Exception {
        when(customerService.getCustomerByAccountId(2)).thenReturn(customer());
        RentalQuoteResponse quote = new RentalQuoteResponse(2, List.of(), BigDecimal.valueOf(400));
        when(rentalService.quoteRental(any())).thenReturn(quote);

        mockMvc.perform(post("/customer/rentals/book").session(createCustomerSession())
                        .param("action", "quote")
                        .param("carIds", "1", "10")
                        .param("pickupDate", LocalDate.now().toString())
                        .param("returnDate", LocalDate.now().plusDays(2).toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("rental/book"))
                .andExpect(model().attribute("quote", quote));

        verify(rentalService, never()).bookCars(any(), any());
    }

    @Test
    @DisplayName("POST /customer/rentals/book - Không chọn xe -> Ở lại form, báo lỗi tại danh sách xe")
    void testBook_NoCarSelected_ShouldStayOnForm() throws Exception {
        when(customerService.getCustomerByAccountId(2)).thenReturn(customer());

        mockMvc.perform(post("/customer/rentals/book").session(createCustomerSession())
                        .param("pickupDate", LocalDate.now().toString())
                        .param("returnDate", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("rental/book"))
                .andExpect(model().attributeHasFieldErrors("rentalRequest", "carIds"));

        verify(rentalService, never()).bookCars(any(), any());
    }

    @Test
    @DisplayName("POST /customer/rentals/book - Ngày trả trước ngày nhận -> Ở lại form, không tạo đơn")
    void testBook_ReturnBeforePickup_ShouldStayOnForm() throws Exception {
        when(customerService.getCustomerByAccountId(2)).thenReturn(customer());

        mockMvc.perform(post("/customer/rentals/book").session(createCustomerSession())
                        .param("carIds", "1")
                        .param("pickupDate", LocalDate.now().plusDays(3).toString())
                        .param("returnDate", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("rental/book"))
                .andExpect(model().attributeHasFieldErrors("rentalRequest", "returnDateAfterPickupDate"));

        verify(rentalService, never()).bookCars(any(), any());
    }

    @Test
    @DisplayName("POST /customer/rentals/book action=book -> Tạo đơn và về lịch sử thuê")
    void testBook_Valid_ShouldRedirectToRentals() throws Exception {
        when(customerService.getCustomerByAccountId(2)).thenReturn(customer());
        when(rentalService.bookCars(eq(5), any())).thenReturn(List.of());

        mockMvc.perform(post("/customer/rentals/book").session(createCustomerSession())
                        .param("action", "book")
                        .param("carIds", "1")
                        .param("pickupDate", LocalDate.now().toString())
                        .param("returnDate", LocalDate.now().plusDays(2).toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customer/rentals"));
    }
}
