package com.project.leapwise.integration;

import com.project.leapwise.db.entity.Account;
import com.project.leapwise.db.entity.Transaction;
import com.project.leapwise.db.repository.AccountRepository;
import com.project.leapwise.db.repository.TransactionRepository;
import com.project.leapwise.dto.TransactionReq;
import com.project.leapwise.dto.TransactionResp;
import com.project.leapwise.type.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Optional;

import static com.project.leapwise.type.AccountType.PERSONAL;
import static java.math.BigDecimal.*;
import static java.math.RoundingMode.HALF_UP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TransactionIntegrationTests extends AbstractIntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private AccountRepository accountRepository;

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
    void createTransaction__noSender() {
        //GIVEN
        final TransactionReq req = new TransactionReq(
                -1L,
                1L,
                ONE,
                1L,
                "Msg"
        );

        //WHEN
        ResponseEntity<TransactionResp> responseEntity = this.restTemplate.postForEntity("http://localhost:" + port + "/transaction", req, TransactionResp.class);

        //THEN
        assertEquals(404, responseEntity.getStatusCode().value());

    }

    @Test
    void createTransaction__noRecipient() {
        //GIVEN
        final TransactionReq req = new TransactionReq(
                1L,
                -1L,
                ONE,
                1L,
                "Msg"
        );

        //WHEN
        ResponseEntity<TransactionResp> responseEntity = this.restTemplate.postForEntity("http://localhost:" + port + "/transaction", req, TransactionResp.class);

        //THEN
        assertEquals(404, responseEntity.getStatusCode().value());

    }

    @Test
    void createTransaction__noBalance() {
        //GIVEN

        final Account senderBefore = accountRepository.findById(1L).get();
        senderBefore.setBalance(ZERO);
        accountRepository.save(senderBefore);
        final Account recipientBefore = accountRepository.findById(2L).get();
        final TransactionReq req = new TransactionReq(
                1L,
                2L,
                ONE,
                1L,
                "Msg"
        );

        //WHEN
        ResponseEntity<TransactionResp> responseEntity = this.restTemplate.postForEntity("http://localhost:" + port + "/transaction", req, TransactionResp.class);

        //THEN
        assertEquals(200, responseEntity.getStatusCode().value());
        final Optional<Transaction> transaction = transactionRepository.findById(responseEntity.getBody().transactionId());
        assertTrue(transaction.isPresent());
        assertEquals(TransactionStatus.FAILED, transaction.get().getTransactionStatus());
        assertEquals(ONE.setScale(2, HALF_UP), transaction.get().getAmount().setScale(2, HALF_UP));

        final Account senderAfter = accountRepository.findById(senderBefore.getAccountId()).get();
        assertEquals(senderBefore.getBalance().setScale(2, HALF_UP), senderAfter.getBalance().setScale(2, HALF_UP));

        final Account recipientAfter = accountRepository.findById(recipientBefore.getAccountId()).get();
        assertEquals(recipientBefore.getBalance().setScale(2, HALF_UP), recipientAfter.getBalance().setScale(2, HALF_UP));
    }

    @Test
    void createTransaction() {
        //GIVEN
        final Account senderBefore = accountRepository.findById(1L).get();
        final Account recipientBefore = accountRepository.findById(2L).get();
        final TransactionReq req = new TransactionReq(
                1L,
                2L,
                ONE,
                1L,
                "Msg"
        );

        //WHEN
        ResponseEntity<TransactionResp> responseEntity = this.restTemplate.postForEntity("http://localhost:" + port + "/transaction", req, TransactionResp.class);

        //THEN
        assertEquals(200, responseEntity.getStatusCode().value());
        final Optional<Transaction> transaction = transactionRepository.findById(responseEntity.getBody().transactionId());
        assertTrue(transaction.isPresent());
        assertEquals(TransactionStatus.COMPLETED, transaction.get().getTransactionStatus());
        assertEquals(ONE.setScale(2, HALF_UP), transaction.get().getAmount().setScale(2, HALF_UP));

        final Account senderAfter = accountRepository.findById(senderBefore.getAccountId()).get();
        assertEquals(senderBefore.getBalance().subtract(ONE).setScale(2, HALF_UP), senderAfter.getBalance().setScale(2, HALF_UP));

        final Account recipientAfter = accountRepository.findById(recipientBefore.getAccountId()).get();
        assertEquals(recipientBefore.getBalance().add(ONE).setScale(2, HALF_UP), recipientAfter.getBalance().setScale(2, HALF_UP));

        assertEquals(senderBefore.getBalance().add(recipientBefore.getBalance()).setScale(2, HALF_UP),
                senderAfter.getBalance().add(recipientAfter.getBalance()).setScale(2, HALF_UP));
    }
}
