package com.hsf.fucarrenting.auth.service;

import com.hsf.fucarrenting.auth.dto.RegisterRequest;
import com.hsf.fucarrenting.auth.entity.Account;
import com.hsf.fucarrenting.auth.entity.Role;
import com.hsf.fucarrenting.auth.mapper.AccountMapper;
import com.hsf.fucarrenting.auth.repository.AccountRepository;
import com.hsf.fucarrenting.common.exception.BusinessException;
import com.hsf.fucarrenting.customer.entity.Customer;
import com.hsf.fucarrenting.customer.mapper.CustomerMapper;
import com.hsf.fucarrenting.customer.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Spy
    private AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);

    @Spy
    private CustomerMapper customerMapper = Mappers.getMapper(CustomerMapper.class);

    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest buildRegisterRequest() {
        return new RegisterRequest(
                "newcustomer",
                "newcustomer@fucar.com",
                "secret123",
                "Trần Thị B",
                "0987654321",
                LocalDate.of(1995, 3, 15),
                "098765432109",
                "B2-222222",
                LocalDate.of(2019, 6, 1)
        );
    }

    @Test
    @DisplayName("register - Mật khẩu được mã hóa BCrypt trước khi lưu")
    void testRegisterEncodesPassword() {
        RegisterRequest request = buildRegisterRequest();
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.register(request);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        String storedPassword = captor.getValue().getPassword();

        assertThat(storedPassword).isNotEqualTo("secret123").startsWith("$2");
        assertThat(passwordEncoder.matches("secret123", storedPassword)).isTrue();
    }

    @Test
    @DisplayName("register - Dữ liệu được trim và map đúng sang Account (role Customer) và Customer")
    void testRegisterMapsAccountAndCustomer() {
        RegisterRequest request = new RegisterRequest(
                "  newcustomer ", " newcustomer@fucar.com ", "secret123",
                " Trần Thị B ", " 0987654321 ", LocalDate.of(1995, 3, 15),
                " 098765432109 ", " B2-222222 ", LocalDate.of(2019, 6, 1));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.register(request);

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountCaptor.capture());
        Account account = accountCaptor.getValue();
        assertThat(account.getAccountName()).isEqualTo("newcustomer");
        assertThat(account.getEmail()).isEqualTo("newcustomer@fucar.com");
        assertThat(account.getRole()).isEqualTo(Role.Customer);

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());
        Customer customer = customerCaptor.getValue();
        assertThat(customer.getFullName()).isEqualTo("Trần Thị B");
        assertThat(customer.getMobile()).isEqualTo("0987654321");
        assertThat(customer.getIdentityCard()).isEqualTo("098765432109");
        assertThat(customer.getLicenceNumber()).isEqualTo("B2-222222");
        assertThat(customer.getBirthday()).isEqualTo(LocalDate.of(1995, 3, 15));
        assertThat(customer.getAccount()).isSameAs(account);
    }

    @Test
    @DisplayName("login - Đăng nhập thành công với mật khẩu BCrypt")
    void testLoginWithBcryptPassword() {
        Account account = Account.builder()
                .accountId(1).accountName("newcustomer").role(Role.Customer)
                .password(passwordEncoder.encode("secret123"))
                .build();
        when(accountRepository.findByAccountName("newcustomer")).thenReturn(Optional.of(account));

        authService.login("newcustomer", "secret123");

        verify(accountMapper).toResponse(account);
    }

    @Test
    @DisplayName("login - Không chấp nhận mật khẩu lưu dạng plain-text")
    void testLoginRejectsPlainTextStoredPassword() {
        Account account = Account.builder()
                .accountId(2).accountName("customer1").role(Role.Customer)
                .password("123456")
                .build();
        when(accountRepository.findByAccountName("customer1")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> authService.login("customer1", "123456"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("login - Sai mật khẩu ném BusinessException")
    void testLoginWithWrongPassword() {
        Account account = Account.builder()
                .accountId(1).accountName("newcustomer").role(Role.Customer)
                .password(passwordEncoder.encode("secret123"))
                .build();
        when(accountRepository.findByAccountName("newcustomer")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> authService.login("newcustomer", "wrong"))
                .isInstanceOf(BusinessException.class);
    }
}
