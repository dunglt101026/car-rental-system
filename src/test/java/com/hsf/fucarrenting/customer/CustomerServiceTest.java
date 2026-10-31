package com.hsf.fucarrenting.customer;

import com.hsf.fucarrenting.auth.Account;
import com.hsf.fucarrenting.auth.AccountRepository;
import com.hsf.fucarrenting.auth.Role;
import com.hsf.fucarrenting.rental.CarRentalRepository;
import com.hsf.fucarrenting.common.exception.BusinessException;
import com.hsf.fucarrenting.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CarRentalRepository carRentalRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer1;
    private Customer customer2;
    private Account account1;

    @BeforeEach
    void setUp() {
        account1 = Account.builder()
                .accountId(1)
                .accountName("customer01")
                .email("customer01@fucar.com")
                .role(Role.Customer)
                .build();

        customer1 = Customer.builder()
                .customerId(1)
                .fullName("Nguyễn Văn A")
                .mobile("0912345678")
                .birthday(LocalDate.of(1990, 5, 10))
                .identityCard("012345678901")
                .licenceNumber("B2-111111")
                .licenceDate(LocalDate.of(2018, 5, 10))
                .account(account1)
                .build();

        customer2 = Customer.builder()
                .customerId(2)
                .fullName("Trần Thị B")
                .mobile("0987654321")
                .birthday(LocalDate.of(1995, 8, 20))
                .identityCard("098765432109")
                .licenceNumber("B2-222222")
                .licenceDate(LocalDate.of(2020, 8, 20))
                .build();
    }

    @Test
    @DisplayName("getAdminCustomers - Tìm kiếm và phân trang khách hàng thành công")
    void testGetAdminCustomers_Success() {
        Page<Customer> page = new PageImpl<>(List.of(customer1));
        when(customerRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        CustomerResponse mockResponse = new CustomerResponse(
                1, "Nguyễn Văn A", "0912345678", LocalDate.of(1990, 5, 10),
                "012345678901", "B2-111111", LocalDate.of(2018, 5, 10),
                1, "customer01", "customer01@fucar.com"
        );
        when(customerMapper.toResponse(customer1)).thenReturn(mockResponse);

        Page<CustomerResponse> result = customerService.getAdminCustomers("Nguyễn", 0, 10);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).fullName()).isEqualTo("Nguyễn Văn A");
    }

    @Test
    @DisplayName("getCustomerById - Tìm thấy khách hàng theo ID")
    void testGetCustomerById_Success() {
        when(customerRepository.findById(1)).thenReturn(Optional.of(customer1));
        CustomerResponse mockResponse = new CustomerResponse(
                1, "Nguyễn Văn A", "0912345678", LocalDate.of(1990, 5, 10),
                "012345678901", "B2-111111", LocalDate.of(2018, 5, 10),
                1, "customer01", "customer01@fucar.com"
        );
        when(customerMapper.toResponse(customer1)).thenReturn(mockResponse);

        CustomerResponse result = customerService.getCustomerById(1);

        assertThat(result).isNotNull();
        assertThat(result.customerId()).isEqualTo(1);
    }

    @Test
    @DisplayName("getCustomerById - Không tìm thấy -> Ném ResourceNotFoundException")
    void testGetCustomerById_NotFound() {
        when(customerRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getCustomerById(999))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("updateCustomer - Cập nhật thông tin khách hàng thành công")
    void testUpdateCustomer_Success() {
        CustomerProfileRequest request = new CustomerProfileRequest(
                "Nguyễn Văn A Updated",
                "0912345678",
                LocalDate.of(1990, 5, 10),
                "012345678901",
                "B2-999999",
                LocalDate.of(2019, 1, 1)
        );

        when(customerRepository.findById(1)).thenReturn(Optional.of(customer1));
        doAnswer(invocation -> {
            customer1.setFullName(request.fullName());
            customer1.setLicenceNumber(request.licenceNumber());
            return null;
        }).when(customerMapper).updateEntityFromProfile(request, customer1);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerResponse mockResponse = new CustomerResponse(
                1, "Nguyễn Văn A Updated", "0912345678", LocalDate.of(1990, 5, 10),
                "012345678901", "B2-999999", LocalDate.of(2019, 1, 1),
                1, "customer01", "customer01@fucar.com"
        );
        when(customerMapper.toResponse(customer1)).thenReturn(mockResponse);

        CustomerResponse response = customerService.updateCustomer(1, request);

        assertThat(response.fullName()).isEqualTo("Nguyễn Văn A Updated");
        assertThat(customer1.getLicenceNumber()).isEqualTo("B2-999999");
        verify(customerRepository, times(1)).save(customer1);
    }

    @Test
    @DisplayName("updateCustomer - Trùng số điện thoại với khách hàng khác -> Ném BusinessException")
    void testUpdateCustomer_DuplicateMobile_ShouldThrowException() {
        CustomerProfileRequest request = new CustomerProfileRequest(
                "Nguyễn Văn A",
                "0987654321", // SĐT của customer2
                LocalDate.of(1990, 5, 10),
                "012345678901",
                "B2-111111",
                LocalDate.of(2018, 5, 10)
        );

        when(customerRepository.findById(1)).thenReturn(Optional.of(customer1));
        when(customerRepository.existsByMobileAndCustomerIdNot("0987654321", 1)).thenReturn(true);

        assertThatThrownBy(() -> customerService.updateCustomer(1, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Số điện thoại '0987654321' đã được sử dụng");

        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateCustomer - Trùng CCCD/CMND với khách hàng khác -> Ném BusinessException")
    void testUpdateCustomer_DuplicateIdentityCard_ShouldThrowException() {
        CustomerProfileRequest request = new CustomerProfileRequest(
                "Nguyễn Văn A",
                "0912345678",
                LocalDate.of(1990, 5, 10),
                "098765432109", // CCCD của customer2
                "B2-111111",
                LocalDate.of(2018, 5, 10)
        );

        when(customerRepository.findById(1)).thenReturn(Optional.of(customer1));
        when(customerRepository.existsByIdentityCardAndCustomerIdNot("098765432109", 1)).thenReturn(true);

        assertThatThrownBy(() -> customerService.updateCustomer(1, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Số CCCD/CMND '098765432109' đã được sử dụng");

        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteCustomer - Khách chưa có đơn thuê -> Xóa khách hàng và tài khoản liên kết")
    void testDeleteCustomer_NoRentals_ShouldDeleteCustomerAndAccount() {
        when(customerRepository.findById(1)).thenReturn(Optional.of(customer1));
        when(carRentalRepository.existsByCustomer_CustomerId(1)).thenReturn(false);

        String fullName = customerService.deleteCustomer(1);

        assertThat(fullName).isEqualTo("Nguyễn Văn A");
        verify(customerRepository).delete(customer1);
        verify(accountRepository).delete(account1);
    }

    @Test
    @DisplayName("deleteCustomer - Khách đã có đơn thuê -> Ném BusinessException, không xóa")
    void testDeleteCustomer_HasRentals_ShouldThrowException() {
        when(customerRepository.findById(1)).thenReturn(Optional.of(customer1));
        when(carRentalRepository.existsByCustomer_CustomerId(1)).thenReturn(true);

        assertThatThrownBy(() -> customerService.deleteCustomer(1))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("đã phát sinh giao dịch thuê xe");

        verify(customerRepository, never()).delete(any(Customer.class));
        verify(accountRepository, never()).delete(any(Account.class));
    }
}
