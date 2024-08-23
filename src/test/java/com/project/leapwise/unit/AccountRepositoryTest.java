package com.project.leapwise.unit;

import com.project.leapwise.db.entity.Account;
import com.project.leapwise.db.entity.Transaction;
import com.project.leapwise.db.repository.AccountRepository;
import com.project.leapwise.db.repository.TransactionRepository;
import com.project.leapwise.type.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import static com.project.leapwise.type.AccountType.PERSONAL;
import static java.math.BigDecimal.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    public void setUp() {
        final Account accountEntity1 = new Account();
        accountEntity1.setAccountId(1L);
        accountEntity1.setBalance(TEN);
        accountEntity1.setPastMonthTurnover(ONE);
        accountEntity1.setAccountType(PERSONAL);
        accountEntity1.setAccountNumber("1");
        accountEntity1.setCustomerId(1L);
        accountRepository.save(accountEntity1);

        final Account accountEntity2 = new Account();
        accountEntity2.setAccountId(2L);
        accountEntity2.setBalance(TEN);
        accountEntity2.setPastMonthTurnover(ONE);
        accountEntity2.setAccountType(PERSONAL);
        accountEntity2.setAccountNumber("2");
        accountEntity2.setCustomerId(2L);
        accountRepository.save(accountEntity2);
    }

    @Test
    void updatePastMonthTurnover__noTransactions() {
        //GIVEN

        //WHEN
        accountRepository.updatePastMonthTurnover();

        //THEN
        final Account account = accountRepository.findById(1L).get();
        assertEquals(ZERO.setScale(2, RoundingMode.HALF_UP), account.getPastMonthTurnover().setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    void updatePastMonthTurnover__noTransactionsLastMonth() {
        //GIVEN
        Transaction transaction = new Transaction();
        transaction.setSenderAccountId(1L);
        transaction.setRecipientAccountId(2L);
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setCurrencyId(1L);
        transaction.setMessage("Test transaction");
        transaction.setTimestamp(LocalDateTime.now().minusMonths(2));
        transaction.setTransactionStatus(TransactionStatus.COMPLETED);
        transactionRepository.saveAndFlush(transaction);


        //WHEN
        accountRepository.updatePastMonthTurnover();

        //THEN
        final Account account = accountRepository.findById(1L).get();
        assertEquals(ZERO.setScale(2, RoundingMode.HALF_UP), account.getPastMonthTurnover().setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    void updatePastMonthTurnover() {
        //GIVEN
        Transaction transaction = new Transaction();
        transaction.setSenderAccountId(1L);
        transaction.setRecipientAccountId(2L);
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setCurrencyId(1L);
        transaction.setMessage("Test transaction");
        transaction.setTimestamp(LocalDateTime.now().minusMonths(1));
        transaction.setTransactionStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(transaction);

        //WHEN
        accountRepository.updatePastMonthTurnover();

        //THEN
        final Account accountSender = accountRepository.findById(1L).get();
        assertEquals(new BigDecimal("-100.00").setScale(2, RoundingMode.HALF_UP), accountSender.getPastMonthTurnover().setScale(2, RoundingMode.HALF_UP));
        final Account accountRecipient = accountRepository.findById(2L).get();
        assertEquals(new BigDecimal("100.00").setScale(2, RoundingMode.HALF_UP), accountRecipient.getPastMonthTurnover().setScale(2, RoundingMode.HALF_UP));

    }


    @Test
    void updatePastMonthTurnover__multipleTransactions() {
        //GIVEN
        Transaction transaction1 = new Transaction();
        transaction1.setSenderAccountId(1L);
        transaction1.setRecipientAccountId(2L);
        transaction1.setAmount(new BigDecimal("100.00"));
        transaction1.setCurrencyId(1L);
        transaction1.setMessage("Test transaction2");
        transaction1.setTimestamp(LocalDateTime.now().minusMonths(1));
        transaction1.setTransactionStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(transaction1);

        Transaction transaction2 = new Transaction();
        transaction2.setSenderAccountId(2L);
        transaction2.setRecipientAccountId(1L);
        transaction2.setAmount(new BigDecimal("10.00"));
        transaction2.setCurrencyId(1L);
        transaction2.setMessage("Test transaction2");
        transaction2.setTimestamp(LocalDateTime.now().minusMonths(1));
        transaction2.setTransactionStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(transaction2);

        //WHEN
        accountRepository.updatePastMonthTurnover();

        //THEN
        final Account accountSender = accountRepository.findById(1L).get();
        assertEquals(new BigDecimal("-90.00").setScale(2, RoundingMode.HALF_UP), accountSender.getPastMonthTurnover().setScale(2, RoundingMode.HALF_UP));
        final Account accountRecipient = accountRepository.findById(2L).get();
        assertEquals(new BigDecimal("90.00").setScale(2, RoundingMode.HALF_UP), accountRecipient.getPastMonthTurnover().setScale(2, RoundingMode.HALF_UP));

    }
}
