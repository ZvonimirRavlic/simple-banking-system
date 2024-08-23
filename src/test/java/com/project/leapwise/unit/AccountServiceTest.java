package com.project.leapwise.unit;

import com.project.leapwise.db.entity.Account;
import com.project.leapwise.db.repository.AccountRepository;
import com.project.leapwise.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static com.project.leapwise.type.AccountType.PERSONAL;
import static java.math.BigDecimal.ONE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class AccountServiceTest {


    @Autowired
    private AccountService accountService;

    @MockBean
    private AccountRepository accountRepository;

    @BeforeEach
    public void reset_mocks() {
        Mockito.reset(accountRepository);
    }


    @Test
    void findById__ResponseStatusException__noAccount() {
        //GIVEN
        when(accountRepository.findById(any())).thenReturn(Optional.empty());

        //WHEN
        final Exception exception = assertThrows(RuntimeException.class, () -> accountService.findById(1L));

        //THEN
        assertEquals("404 NOT_FOUND \"Account with accountId: 1 doesn't exist!\"", exception.getMessage());
    }

    @Test
    void findById__ResponseStatusException__nullId() {
        //GIVEN

        //WHEN
        assertThrows(RuntimeException.class, () -> accountService.findById(null));

        //THEN
    }

    @Test
    void findById() {
        //GIVEN
        final Account accountEntity = new Account();
        accountEntity.setAccountId(1L);
        accountEntity.setBalance(ONE);
        accountEntity.setPastMonthTurnover(ONE);
        accountEntity.setAccountType(PERSONAL);
        accountEntity.setAccountNumber("1");
        accountEntity.setCustomerId(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(accountEntity));

        //WHEN
        final Account accountResp = accountService.findById(1L);

        //THEN
        assertEquals(accountEntity.getAccountId(), accountResp.getAccountId());
        assertEquals(accountEntity.getBalance(), accountResp.getBalance());
        assertEquals(accountEntity.getPastMonthTurnover(), accountResp.getPastMonthTurnover());
        assertEquals(accountEntity.getAccountType(), accountResp.getAccountType());
        assertEquals(accountEntity.getAccountNumber(), accountResp.getAccountNumber());
        assertEquals(accountEntity.getCustomerId(), accountResp.getCustomerId());
    }
}
