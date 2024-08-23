package com.project.leapwise.unit;

import com.project.leapwise.db.entity.Account;
import com.project.leapwise.db.entity.Customer;
import com.project.leapwise.db.repository.AccountRepository;
import com.project.leapwise.db.repository.CustomerRepository;
import com.project.leapwise.dto.AccountResp;
import com.project.leapwise.dto.CustomerResp;
import com.project.leapwise.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.project.leapwise.type.AccountType.PERSONAL;
import static java.math.BigDecimal.ONE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class CustomerServiceTest {


    @Autowired
    private CustomerService customerService;
    @MockBean
    private CustomerRepository customerRepository;
    @MockBean
    private AccountRepository accountRepository;

    @BeforeEach
    public void reset_mocks() {
        Mockito.reset(customerRepository,
                accountRepository);
    }

    @Test
    void getCustomer__ResponseStatusException__noCustomer() {
        //GIVEN
        when(customerRepository.findById(any())).thenReturn(Optional.empty());

        //WHEN
        final Exception exception = assertThrows(RuntimeException.class, () -> customerService.getCustomer(1L));

        //THEN
        assertEquals("404 NOT_FOUND \"Customer with id: 1 doesn't exist!\"", exception.getMessage());
    }

    @Test
    void getCustomer__ResponseStatusException__nullId() {
        //GIVEN

        //WHEN
        assertThrows(RuntimeException.class, () -> customerService.getCustomer(null));

        //THEN
    }

    @Test
    void getCustomer() {
        //GIVEN
        final Customer customer = new Customer();
        customer.setCustomerId(1L);
        customer.setPhoneNumber("+385991938924");
        customer.setAddress("address 123");
        customer.setName("Name");
        customer.setEmail("mail@mail.com");
        when(customerRepository.findById(any())).thenReturn(Optional.of(customer));

        final Account account = new Account();
        account.setAccountId(1L);
        account.setAccountNumber("1");
        account.setCustomerId(1L);
        account.setPastMonthTurnover(ONE);
        account.setBalance(ONE);
        account.setAccountType(PERSONAL);

        when(accountRepository.findByCustomerId(any())).thenReturn(List.of(account));

        //WHEN
        final CustomerResp customerResp = customerService.getCustomer(1L);

        //THEN
        assertEquals(customer.getCustomerId(), customerResp.customerId());
        assertEquals(customer.getName(), customerResp.name());
        assertEquals(customer.getAddress(), customerResp.address());
        assertEquals(customer.getEmail(), customerResp.email());
        assertEquals(customer.getPhoneNumber(), customerResp.phoneNumber());
        assertEquals(ONE, customerResp.totalBalance());
        assertEquals(1, customerResp.accounts().size());

        final AccountResp accountResp = customerResp.accounts().get(0);

        assertEquals(account.getAccountId(), accountResp.accountId());
        assertEquals(account.getAccountNumber(), accountResp.accountNumber());
        assertEquals(account.getAccountType().name(), accountResp.accountType());
        assertEquals(account.getBalance(), accountResp.balance());
        assertEquals(account.getPastMonthTurnover(), accountResp.pastMonthTurnover());
    }

    @Test
    void getCustomer__multipleAccounts() {
        //GIVEN
        final Customer customer = new Customer();
        customer.setCustomerId(1L);
        customer.setPhoneNumber("+385991938924");
        customer.setAddress("address 123");
        customer.setName("Name");
        customer.setEmail("mail@mail.com");
        when(customerRepository.findById(any())).thenReturn(Optional.of(customer));

        final Account account1 = new Account();
        account1.setAccountId(1L);
        account1.setAccountNumber("1");
        account1.setCustomerId(1L);
        account1.setPastMonthTurnover(ONE);
        account1.setBalance(ONE);
        account1.setAccountType(PERSONAL);

        final Account account2 = new Account();
        account2.setAccountId(1L);
        account2.setAccountNumber("1");
        account2.setCustomerId(1L);
        account2.setPastMonthTurnover(ONE);
        account2.setBalance(ONE);
        account2.setAccountType(PERSONAL);

        when(accountRepository.findByCustomerId(any())).thenReturn(List.of(account1, account2));

        //WHEN
        final CustomerResp customerResp = customerService.getCustomer(1L);

        //THEN
        assertEquals(2, customerResp.accounts().size());
        assertEquals(BigDecimal.valueOf(2), customerResp.totalBalance());
    }

    @Test
    void findById__ResponseStatusException__noCustomer() {
        //GIVEN
        when(customerRepository.findById(any())).thenReturn(Optional.empty());

        //WHEN
        final Exception exception = assertThrows(RuntimeException.class, () -> customerService.findById(1L));

        //THEN
        assertEquals("404 NOT_FOUND \"Customer with id: 1 doesn't exist!\"", exception.getMessage());
    }

    @Test
    void findById__ResponseStatusException__nullId() {
        //GIVEN

        //WHEN
        assertThrows(RuntimeException.class, () -> customerService.findById(null));

        //THEN
    }

    @Test
    void findById() {
        //GIVEN
        final Customer customerEntity = new Customer();
        customerEntity.setCustomerId(1L);
        customerEntity.setPhoneNumber("+385991938924");
        customerEntity.setAddress("address 123");
        customerEntity.setName("Name");
        customerEntity.setEmail("mail@mail.com");
        when(customerRepository.findById(any())).thenReturn(Optional.of(customerEntity));

        //WHEN
        final Customer customerResp = customerService.findById(1L);

        //THEN
        assertEquals(customerEntity.getCustomerId(), customerResp.getCustomerId());
        assertEquals(customerEntity.getName(), customerResp.getName());
        assertEquals(customerEntity.getAddress(), customerResp.getAddress());
        assertEquals(customerEntity.getEmail(), customerResp.getEmail());
        assertEquals(customerEntity.getPhoneNumber(), customerResp.getPhoneNumber());
    }

}
