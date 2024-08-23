package com.project.leapwise.unit;

import com.project.leapwise.db.entity.Account;
import com.project.leapwise.db.entity.Customer;
import com.project.leapwise.db.entity.Transaction;
import com.project.leapwise.db.repository.AccountRepository;
import com.project.leapwise.db.repository.CustomerRepository;
import com.project.leapwise.db.repository.TransactionRepository;
import com.project.leapwise.dto.TransactionDetailedResp;
import com.project.leapwise.dto.TransactionReq;
import com.project.leapwise.dto.TransactionResp;
import com.project.leapwise.service.EmailService;
import com.project.leapwise.service.TransactionService;
import com.project.leapwise.type.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.project.leapwise.type.AccountType.PERSONAL;
import static java.math.BigDecimal.*;
import static java.math.RoundingMode.HALF_UP;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;
    @SpyBean
    private TransactionRepository transactionRepository;
    @SpyBean
    private AccountRepository accountRepository;
    @MockBean
    private CustomerRepository customerRepository;
    @MockBean
    private EmailService emailService;

    @BeforeEach
    public void reset_mocks() {
        Mockito.reset(transactionRepository, accountRepository);
    }

    @BeforeEach
    public void setUp() {
        final Customer customer1 = new Customer();
        customer1.setCustomerId(1L);
        customer1.setPhoneNumber("+385991938924");
        customer1.setAddress("address 123");
        customer1.setName("Name");
        customer1.setEmail("mail@mail.com");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer1));

        final Customer customer2 = new Customer();
        customer2.setCustomerId(1L);
        customer2.setPhoneNumber("+385991938924");
        customer2.setAddress("address 123");
        customer2.setName("Name");
        customer2.setEmail("mail@mail.com");
        when(customerRepository.findById(2L)).thenReturn(Optional.of(customer2));
    }


    @Test
    void createTransaction__noSenderAccount() {
        //GIVEN
        when(accountRepository.findById(-1L)).thenReturn(Optional.empty());
        final Account accountBefore = new Account();
        accountBefore.setBalance(TEN);
        accountBefore.setPastMonthTurnover(ONE);
        accountBefore.setAccountType(PERSONAL);
        accountBefore.setAccountNumber("1");
        accountBefore.setCustomerId(1L);
        accountRepository.save(accountBefore);

        final TransactionReq transactionReq = new TransactionReq(
                -1L,
                accountBefore.getAccountId(),
                new BigDecimal("100.00"),
                1L,
                "Test transaction");

        //WHEN
        final Exception exception = assertThrows(RuntimeException.class, () -> transactionService.createTransaction(transactionReq));

        //THEN
        assertEquals("404 NOT_FOUND \"Account with accountId: -1 doesn't exist!\"", exception.getMessage());
    }


    @Test
    void createTransaction__noRecipientAccount() {
        //GIVEN
        final Account accountBefore = new Account();
        accountBefore.setBalance(TEN);
        accountBefore.setPastMonthTurnover(ONE);
        accountBefore.setAccountType(PERSONAL);
        accountBefore.setAccountNumber("1");
        accountBefore.setCustomerId(1L);
        accountRepository.save(accountBefore);
        when(accountRepository.findById(-1L)).thenReturn(Optional.empty());

        final TransactionReq transactionReq = new TransactionReq(
                accountBefore.getAccountId(),
                -1L,
                new BigDecimal("100.00"),
                1L,
                "Test transaction");

        //WHEN
        final Exception exception = assertThrows(RuntimeException.class, () -> transactionService.createTransaction(transactionReq));

        //THEN
        assertEquals("404 NOT_FOUND \"Account with accountId: -1 doesn't exist!\"", exception.getMessage());

    }

    @Test
    void createTransaction__insufficientBalance() {
        //GIVEN
        final Account accountBefore1 = new Account();
        accountBefore1.setBalance(TEN);
        accountBefore1.setPastMonthTurnover(ONE);
        accountBefore1.setAccountType(PERSONAL);
        accountBefore1.setAccountNumber("1");
        accountBefore1.setCustomerId(1L);
        accountRepository.save(accountBefore1);

        final Account accountBefore2 = new Account();
        accountBefore2.setBalance(ONE);
        accountBefore2.setPastMonthTurnover(ONE);
        accountBefore2.setAccountType(PERSONAL);
        accountBefore2.setAccountNumber("1");
        accountBefore2.setCustomerId(1L);
        accountRepository.save(accountBefore2);

        final TransactionReq transactionReq = new TransactionReq(
                accountBefore1.getAccountId(),
                accountBefore2.getAccountId(),
                new BigDecimal("100.00"),
                1L,
                "Test transaction");

        //WHEN
        final TransactionResp transactionResp = transactionService.createTransaction(transactionReq);

        //THEN
        final Transaction transactionEntity = transactionRepository.findById(transactionResp.transactionId()).get();
        assertEquals(TransactionStatus.FAILED, transactionEntity.getTransactionStatus());

        final Account accountAfter1 = accountRepository.findById(accountBefore1.getAccountId()).get();
        assertEquals(accountBefore1.getBalance().setScale(2, HALF_UP), accountAfter1.getBalance().setScale(2, HALF_UP));

        final Account accountAfter2 = accountRepository.findById(accountBefore2.getAccountId()).get();
        assertEquals(accountBefore2.getBalance().setScale(2, HALF_UP), accountAfter2.getBalance().setScale(2, HALF_UP));

    }

    @Test
    void createTransaction() {
        //GIVEN
        final Account accountBefore1 = new Account();
        accountBefore1.setBalance(TEN);
        accountBefore1.setPastMonthTurnover(ONE);
        accountBefore1.setAccountType(PERSONAL);
        accountBefore1.setAccountNumber("1");
        accountBefore1.setCustomerId(1L);
        accountRepository.save(accountBefore1);

        final Account accountBefore2 = new Account();
        accountBefore2.setBalance(ONE);
        accountBefore2.setPastMonthTurnover(ONE);
        accountBefore2.setAccountType(PERSONAL);
        accountBefore2.setAccountNumber("1");
        accountBefore2.setCustomerId(1L);
        accountRepository.save(accountBefore2);

        final TransactionReq transactionReq = new TransactionReq(
                accountBefore1.getAccountId(),
                accountBefore2.getAccountId(),
                TEN,
                1L,
                "Test transaction");

        //WHEN
        final TransactionResp transactionResp = transactionService.createTransaction(transactionReq);

        //THEN
        final Transaction transactionEntity = transactionRepository.findById(transactionResp.transactionId()).get();
        assertEquals(TransactionStatus.COMPLETED, transactionEntity.getTransactionStatus());

        final Account accountAfter1 = accountRepository.findById(accountBefore1.getAccountId()).get();
        assertEquals(ZERO.setScale(2, HALF_UP), accountAfter1.getBalance().setScale(2, HALF_UP));

        final Account accountAfter2 = accountRepository.findById(accountBefore2.getAccountId()).get();
        assertEquals(BigDecimal.valueOf(11).setScale(2, HALF_UP), accountAfter2.getBalance().setScale(2, HALF_UP));

        assertEquals(accountBefore1.getBalance().add(accountBefore2.getBalance()).setScale(2, HALF_UP),
                accountAfter1.getBalance().add(accountAfter2.getBalance()).setScale(2, HALF_UP));
    }

    @Test
    void transactionHistory__noTransactions() {
        //GIVEN
        when(transactionRepository.findByCustomerId(any())).thenReturn(List.of());

        //WHEN
        Page<TransactionDetailedResp> transactions = transactionService.transactionHistory(1L, new HashMap<>(), Pageable.ofSize(1));

        //THEN
        assertEquals(0L, transactions.getTotalElements());

    }

    @Test
    void transactionHistory() {
        //GIVEN
        final Account accountEntity1 = new Account();
        accountEntity1.setAccountId(1L);
        accountEntity1.setBalance(TEN);
        accountEntity1.setPastMonthTurnover(ONE);
        accountEntity1.setAccountType(PERSONAL);
        accountEntity1.setAccountNumber("1");
        accountEntity1.setCustomerId(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(accountEntity1));

        final Account accountEntity2 = new Account();
        accountEntity2.setAccountId(2L);
        accountEntity2.setBalance(TEN);
        accountEntity2.setPastMonthTurnover(ONE);
        accountEntity2.setAccountType(PERSONAL);
        accountEntity2.setAccountNumber("2");
        accountEntity2.setCustomerId(2L);
        when(accountRepository.findById(2L)).thenReturn(Optional.of(accountEntity2));

        final Transaction transactionEntity = new Transaction();
        transactionEntity.setSenderAccountId(1L);
        transactionEntity.setRecipientAccountId(2L);
        transactionEntity.setAmount(new BigDecimal("100.00"));
        transactionEntity.setCurrencyId(1L);
        transactionEntity.setMessage("Test transactionEntity");
        transactionEntity.setTimestamp(LocalDateTime.now());
        transactionEntity.setTransactionStatus(TransactionStatus.COMPLETED);
        when(transactionRepository.findByCustomerId(any(), any())).thenReturn(new PageImpl<Transaction>(List.of(transactionEntity)));

        //WHEN
        final Page<TransactionDetailedResp> transactions = transactionService.transactionHistory(1L, new HashMap<>(), Pageable.ofSize(1));

        //THEN
        assertEquals(1L, transactions.getTotalElements());

        final TransactionDetailedResp transactionResp = transactions.getContent().get(0);
        assertEquals(accountEntity1.getAccountNumber(), transactionResp.senderAccount().accountNumber());
        assertEquals(accountEntity2.getAccountNumber(), transactionResp.recipientAccount().accountNumber());
        assertEquals(transactionEntity.getAmount().setScale(2, HALF_UP), transactionResp.amount().setScale(2, HALF_UP));
        assertEquals(transactionEntity.getCurrencyId(), transactionResp.currencyId());
        assertEquals(transactionEntity.getMessage(), transactionResp.message());
        assertEquals(transactionEntity.getTimestamp().truncatedTo(DAYS), transactionResp.timestamp().truncatedTo(DAYS));
        assertEquals(transactionEntity.getTransactionStatus().name(), transactionResp.transactionStatus());

    }

    @Test
    void transactionHistory__filterByPageSize() {
        //GIVEN
        final Account accountEntity1 = new Account();
        accountEntity1.setAccountId(1L);
        accountEntity1.setBalance(TEN);
        accountEntity1.setPastMonthTurnover(ONE);
        accountEntity1.setAccountType(PERSONAL);
        accountEntity1.setAccountNumber("1");
        accountEntity1.setCustomerId(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(accountEntity1));

        final Account accountEntity2 = new Account();
        accountEntity2.setAccountId(2L);
        accountEntity2.setBalance(TEN);
        accountEntity2.setPastMonthTurnover(ONE);
        accountEntity2.setAccountType(PERSONAL);
        accountEntity2.setAccountNumber("2");
        accountEntity2.setCustomerId(2L);
        when(accountRepository.findById(2L)).thenReturn(Optional.of(accountEntity2));

        final Transaction transactionEntity1 = new Transaction();
        transactionEntity1.setSenderAccountId(1L);
        transactionEntity1.setRecipientAccountId(2L);
        transactionEntity1.setAmount(new BigDecimal("100.00"));
        transactionEntity1.setCurrencyId(1L);
        transactionEntity1.setMessage("Test transactionEntity1");
        transactionEntity1.setTimestamp(LocalDateTime.now());
        transactionEntity1.setTransactionStatus(TransactionStatus.COMPLETED);

        final Transaction transactionEntity2 = new Transaction();
        transactionEntity2.setSenderAccountId(1L);
        transactionEntity2.setRecipientAccountId(2L);
        transactionEntity2.setAmount(new BigDecimal("100.00"));
        transactionEntity2.setCurrencyId(1L);
        transactionEntity2.setMessage("Test transactionEntity2");
        transactionEntity2.setTimestamp(LocalDateTime.now());
        transactionEntity2.setTransactionStatus(TransactionStatus.COMPLETED);
        when(transactionRepository.findByCustomerId(any(), any())).thenReturn(new PageImpl<Transaction>(List.of(transactionEntity1, transactionEntity2)));

        //WHEN
        final Page<TransactionDetailedResp> transactions = transactionService.transactionHistory(1L, new HashMap<>(), Pageable.ofSize(1));

        //THEN
        assertEquals(1L, transactions.getSize());
    }


    @Test
    void transactionHistory__filterByAmount() {
        //GIVEN
        final Account accountEntity1 = new Account();
        accountEntity1.setAccountId(1L);
        accountEntity1.setBalance(TEN);
        accountEntity1.setPastMonthTurnover(ONE);
        accountEntity1.setAccountType(PERSONAL);
        accountEntity1.setAccountNumber("1");
        accountEntity1.setCustomerId(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(accountEntity1));

        final Account accountEntity2 = new Account();
        accountEntity2.setAccountId(2L);
        accountEntity2.setBalance(TEN);
        accountEntity2.setPastMonthTurnover(ONE);
        accountEntity2.setAccountType(PERSONAL);
        accountEntity2.setAccountNumber("2");
        accountEntity2.setCustomerId(2L);
        when(accountRepository.findById(2L)).thenReturn(Optional.of(accountEntity2));

        final Transaction transactionEntity1 = new Transaction();
        transactionEntity1.setSenderAccountId(1L);
        transactionEntity1.setRecipientAccountId(2L);
        transactionEntity1.setAmount(new BigDecimal("100.00"));
        transactionEntity1.setCurrencyId(1L);
        transactionEntity1.setMessage("Test transactionEntity1");
        transactionEntity1.setTimestamp(LocalDateTime.now());
        transactionEntity1.setTransactionStatus(TransactionStatus.COMPLETED);

        final Transaction transactionEntity2 = new Transaction();
        transactionEntity2.setSenderAccountId(1L);
        transactionEntity2.setRecipientAccountId(2L);
        transactionEntity2.setAmount(new BigDecimal("10.00"));
        transactionEntity2.setCurrencyId(1L);
        transactionEntity2.setMessage("Test transactionEntity2");
        transactionEntity2.setTimestamp(LocalDateTime.now());
        transactionEntity2.setTransactionStatus(TransactionStatus.COMPLETED);
        when(transactionRepository.findByCustomerId(any(), any())).thenReturn(new PageImpl<Transaction>(List.of(transactionEntity1, transactionEntity2)));

        final Map<String, String> filterParams = new HashMap<>();
        filterParams.put("amount", "100");

        //WHEN
        final Page<TransactionDetailedResp> transactions = transactionService.transactionHistory(1L, filterParams, Pageable.ofSize(2));

        //THEN
        assertEquals(1L, transactions.getContent().size());
        final TransactionDetailedResp transactionResp = transactions.getContent().get(0);
        assertEquals(new BigDecimal(filterParams.get("amount")).setScale(2, HALF_UP), transactionResp.amount().setScale(2, HALF_UP));

    }

    @Test
    void transactionHistory__filterByCurrencyId() {
        //GIVEN
        final Account accountEntity1 = new Account();
        accountEntity1.setAccountId(1L);
        accountEntity1.setBalance(TEN);
        accountEntity1.setPastMonthTurnover(ONE);
        accountEntity1.setAccountType(PERSONAL);
        accountEntity1.setAccountNumber("1");
        accountEntity1.setCustomerId(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(accountEntity1));

        final Account accountEntity2 = new Account();
        accountEntity2.setAccountId(2L);
        accountEntity2.setBalance(TEN);
        accountEntity2.setPastMonthTurnover(ONE);
        accountEntity2.setAccountType(PERSONAL);
        accountEntity2.setAccountNumber("2");
        accountEntity2.setCustomerId(2L);
        when(accountRepository.findById(2L)).thenReturn(Optional.of(accountEntity2));

        final Transaction transactionEntity1 = new Transaction();
        transactionEntity1.setSenderAccountId(1L);
        transactionEntity1.setRecipientAccountId(2L);
        transactionEntity1.setAmount(new BigDecimal("100.00"));
        transactionEntity1.setCurrencyId(1L);
        transactionEntity1.setMessage("Test transactionEntity1");
        transactionEntity1.setTimestamp(LocalDateTime.now());
        transactionEntity1.setTransactionStatus(TransactionStatus.COMPLETED);

        final Transaction transactionEntity2 = new Transaction();
        transactionEntity2.setSenderAccountId(1L);
        transactionEntity2.setRecipientAccountId(2L);
        transactionEntity2.setAmount(new BigDecimal("10.00"));
        transactionEntity2.setCurrencyId(2L);
        transactionEntity2.setMessage("Test transactionEntity2");
        transactionEntity2.setTimestamp(LocalDateTime.now());
        transactionEntity2.setTransactionStatus(TransactionStatus.COMPLETED);
        when(transactionRepository.findByCustomerId(any(), any())).thenReturn(new PageImpl<Transaction>(List.of(transactionEntity1, transactionEntity2)));

        final Map<String, String> filterParams = new HashMap<>();
        filterParams.put("currencyId", "1");

        //WHEN
        final Page<TransactionDetailedResp> transactions = transactionService.transactionHistory(1L, filterParams, Pageable.ofSize(2));

        //THEN
        assertEquals(1L, transactions.getContent().size());
        final TransactionDetailedResp transactionResp = transactions.getContent().get(0);
        assertEquals(Long.valueOf(filterParams.get("currencyId")), transactionResp.currencyId());
    }

    @Test
    void transactionHistory__filterByTimestamp() {
        //GIVEN
        final Account accountEntity1 = new Account();
        accountEntity1.setAccountId(1L);
        accountEntity1.setBalance(TEN);
        accountEntity1.setPastMonthTurnover(ONE);
        accountEntity1.setAccountType(PERSONAL);
        accountEntity1.setAccountNumber("1");
        accountEntity1.setCustomerId(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(accountEntity1));

        final Account accountEntity2 = new Account();
        accountEntity2.setAccountId(2L);
        accountEntity2.setBalance(TEN);
        accountEntity2.setPastMonthTurnover(ONE);
        accountEntity2.setAccountType(PERSONAL);
        accountEntity2.setAccountNumber("2");
        accountEntity2.setCustomerId(2L);
        when(accountRepository.findById(2L)).thenReturn(Optional.of(accountEntity2));

        final Transaction transactionEntity1 = new Transaction();
        transactionEntity1.setSenderAccountId(1L);
        transactionEntity1.setRecipientAccountId(2L);
        transactionEntity1.setAmount(new BigDecimal("100.00"));
        transactionEntity1.setCurrencyId(1L);
        transactionEntity1.setMessage("Test transactionEntity1");
        transactionEntity1.setTimestamp(LocalDateTime.now().minusMonths(1));
        transactionEntity1.setTransactionStatus(TransactionStatus.COMPLETED);

        final Transaction transactionEntity2 = new Transaction();
        transactionEntity2.setSenderAccountId(1L);
        transactionEntity2.setRecipientAccountId(2L);
        transactionEntity2.setAmount(new BigDecimal("10.00"));
        transactionEntity2.setCurrencyId(2L);
        transactionEntity2.setMessage("Test transactionEntity2");
        transactionEntity2.setTimestamp(LocalDateTime.now());
        transactionEntity2.setTransactionStatus(TransactionStatus.COMPLETED);
        when(transactionRepository.findByCustomerId(any(), any())).thenReturn(new PageImpl<Transaction>(List.of(transactionEntity1, transactionEntity2)));

        final Map<String, String> filterParams = new HashMap<>();
        filterParams.put("timestamp", transactionEntity2.getTimestamp().toString());

        //WHEN
        final Page<TransactionDetailedResp> transactions = transactionService.transactionHistory(1L, filterParams, Pageable.ofSize(2));

        //THEN
        assertEquals(1L, transactions.getContent().size());
        final TransactionDetailedResp transactionResp = transactions.getContent().get(0);
        assertEquals(filterParams.get("timestamp"), transactionResp.timestamp().toString());
    }


    @Test
    void transactionHistory__filterByMessage() {
        //GIVEN
        final Account accountEntity1 = new Account();
        accountEntity1.setAccountId(1L);
        accountEntity1.setBalance(TEN);
        accountEntity1.setPastMonthTurnover(ONE);
        accountEntity1.setAccountType(PERSONAL);
        accountEntity1.setAccountNumber("1");
        accountEntity1.setCustomerId(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(accountEntity1));

        final Account accountEntity2 = new Account();
        accountEntity2.setAccountId(2L);
        accountEntity2.setBalance(TEN);
        accountEntity2.setPastMonthTurnover(ONE);
        accountEntity2.setAccountType(PERSONAL);
        accountEntity2.setAccountNumber("2");
        accountEntity2.setCustomerId(2L);
        when(accountRepository.findById(2L)).thenReturn(Optional.of(accountEntity2));

        final Transaction transactionEntity1 = new Transaction();
        transactionEntity1.setSenderAccountId(1L);
        transactionEntity1.setRecipientAccountId(2L);
        transactionEntity1.setAmount(new BigDecimal("100.00"));
        transactionEntity1.setCurrencyId(1L);
        transactionEntity1.setMessage("Test transactionEntity1");
        transactionEntity1.setTimestamp(LocalDateTime.now());
        transactionEntity1.setTransactionStatus(TransactionStatus.COMPLETED);

        final Transaction transactionEntity2 = new Transaction();
        transactionEntity2.setSenderAccountId(1L);
        transactionEntity2.setRecipientAccountId(2L);
        transactionEntity2.setAmount(new BigDecimal("10.00"));
        transactionEntity2.setCurrencyId(2L);
        transactionEntity2.setMessage("Test transactionEntity2");
        transactionEntity2.setTimestamp(LocalDateTime.now());
        transactionEntity2.setTransactionStatus(TransactionStatus.COMPLETED);
        when(transactionRepository.findByCustomerId(any(), any())).thenReturn(new PageImpl<Transaction>(List.of(transactionEntity1, transactionEntity2)));

        final Map<String, String> filterParams = new HashMap<>();
        filterParams.put("message", "transactionEntity2");

        //WHEN
        final Page<TransactionDetailedResp> transactions = transactionService.transactionHistory(1L, filterParams, Pageable.ofSize(2));

        //THEN
        assertEquals(1L, transactions.getContent().size());
        final TransactionDetailedResp transactionResp = transactions.getContent().get(0);
        assertTrue(transactionResp.message().contains(filterParams.get("message")));
    }

    @Test
    void transactionHistory__filterByTransactionStatus() {
        //GIVEN
        final Account accountEntity1 = new Account();
        accountEntity1.setAccountId(1L);
        accountEntity1.setBalance(TEN);
        accountEntity1.setPastMonthTurnover(ONE);
        accountEntity1.setAccountType(PERSONAL);
        accountEntity1.setAccountNumber("1");
        accountEntity1.setCustomerId(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(accountEntity1));

        final Account accountEntity2 = new Account();
        accountEntity2.setAccountId(2L);
        accountEntity2.setBalance(TEN);
        accountEntity2.setPastMonthTurnover(ONE);
        accountEntity2.setAccountType(PERSONAL);
        accountEntity2.setAccountNumber("2");
        accountEntity2.setCustomerId(2L);
        when(accountRepository.findById(2L)).thenReturn(Optional.of(accountEntity2));

        final Transaction transactionEntity1 = new Transaction();
        transactionEntity1.setSenderAccountId(1L);
        transactionEntity1.setRecipientAccountId(2L);
        transactionEntity1.setAmount(new BigDecimal("100.00"));
        transactionEntity1.setCurrencyId(1L);
        transactionEntity1.setMessage("Test transactionEntity1");
        transactionEntity1.setTimestamp(LocalDateTime.now());
        transactionEntity1.setTransactionStatus(TransactionStatus.PENDING);

        final Transaction transactionEntity2 = new Transaction();
        transactionEntity2.setSenderAccountId(1L);
        transactionEntity2.setRecipientAccountId(2L);
        transactionEntity2.setAmount(new BigDecimal("10.00"));
        transactionEntity2.setCurrencyId(2L);
        transactionEntity2.setMessage("Test transactionEntity2");
        transactionEntity2.setTimestamp(LocalDateTime.now());
        transactionEntity2.setTransactionStatus(TransactionStatus.COMPLETED);
        when(transactionRepository.findByCustomerId(any(), any())).thenReturn(new PageImpl<Transaction>(List.of(transactionEntity1, transactionEntity2)));

        final Map<String, String> filterParams = new HashMap<>();
        filterParams.put("transactionStatus", TransactionStatus.PENDING.name());

        //WHEN
        final Page<TransactionDetailedResp> transactions = transactionService.transactionHistory(1L, filterParams, Pageable.ofSize(2));

        //THEN
        assertEquals(1L, transactions.getContent().size());
        final TransactionDetailedResp transactionResp = transactions.getContent().get(0);
        assertEquals(filterParams.get("transactionStatus"), transactionResp.transactionStatus());
    }

    @Test
    void transactionHistory__filterByAccountId() {
        //GIVEN
        final Account accountEntity1 = new Account();
        accountEntity1.setAccountId(1L);
        accountEntity1.setBalance(TEN);
        accountEntity1.setPastMonthTurnover(ONE);
        accountEntity1.setAccountType(PERSONAL);
        accountEntity1.setAccountNumber("1");
        accountEntity1.setCustomerId(1L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(accountEntity1));

        final Account accountEntity2 = new Account();
        accountEntity2.setAccountId(2L);
        accountEntity2.setBalance(TEN);
        accountEntity2.setPastMonthTurnover(ONE);
        accountEntity2.setAccountType(PERSONAL);
        accountEntity2.setAccountNumber("2");
        accountEntity2.setCustomerId(2L);
        when(accountRepository.findById(2L)).thenReturn(Optional.of(accountEntity2));

        final Transaction transactionEntity1 = new Transaction();
        transactionEntity1.setSenderAccountId(1L);
        transactionEntity1.setRecipientAccountId(1L);
        transactionEntity1.setAmount(new BigDecimal("100.00"));
        transactionEntity1.setCurrencyId(1L);
        transactionEntity1.setMessage("Test transactionEntity1");
        transactionEntity1.setTimestamp(LocalDateTime.now());
        transactionEntity1.setTransactionStatus(TransactionStatus.PENDING);

        final Transaction transactionEntity2 = new Transaction();
        transactionEntity2.setSenderAccountId(2L);
        transactionEntity2.setRecipientAccountId(2L);
        transactionEntity2.setAmount(new BigDecimal("10.00"));
        transactionEntity2.setCurrencyId(2L);
        transactionEntity2.setMessage("Test transactionEntity2");
        transactionEntity2.setTimestamp(LocalDateTime.now());
        transactionEntity2.setTransactionStatus(TransactionStatus.COMPLETED);

        when(transactionRepository.findByCustomerId(any(), any())).thenReturn(new PageImpl<Transaction>(List.of(transactionEntity1, transactionEntity2)));

        final Map<String, String> filterParams = new HashMap<>();
        filterParams.put("accountId", String.valueOf(1));

        //WHEN
        final Page<TransactionDetailedResp> transactions = transactionService.transactionHistory(1L, filterParams, Pageable.ofSize(2));

        //THEN
        assertEquals(1L, transactions.getContent().size());
        final TransactionDetailedResp transactionResp = transactions.getContent().get(0);
        assertEquals(Long.valueOf(filterParams.get("accountId")), transactionResp.senderAccount().accountId());
    }
}
