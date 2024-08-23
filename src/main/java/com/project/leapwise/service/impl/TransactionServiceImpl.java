package com.project.leapwise.service.impl;

import com.project.leapwise.db.entity.Account;
import com.project.leapwise.db.entity.Transaction;
import com.project.leapwise.db.repository.TransactionRepository;
import com.project.leapwise.dto.TransactionDetailedResp;
import com.project.leapwise.dto.TransactionReq;
import com.project.leapwise.dto.TransactionResp;
import com.project.leapwise.mapper.TransactionMapper;
import com.project.leapwise.service.AccountService;
import com.project.leapwise.service.EmailService;
import com.project.leapwise.service.TransactionService;
import com.project.leapwise.type.TransactionStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static com.project.leapwise.type.TransactionStatus.COMPLETED;
import static com.project.leapwise.type.TransactionStatus.FAILED;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionMapper transactionMapper;
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final EmailService emailService;


    @Transactional
    @Override
    public TransactionResp createTransaction(TransactionReq transactionReq) {
        final Transaction newTransaction = transactionMapper.toTransactionEntity(transactionReq);
        transactionRepository.save(newTransaction);
        processTransaction(newTransaction.getTransactionId());
        return transactionMapper.toTransactionResp(newTransaction);
    }

    public void processTransaction(final Long transactionId) {

        final Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResponseStatusException(INTERNAL_SERVER_ERROR));

        final Account senderAccount = accountService.findById(transaction.getSenderAccountId());
        final Account recepientAccount = accountService.findById(transaction.getRecipientAccountId());

        if (senderAccount.getBalance().compareTo(transaction.getAmount()) < 0) {
            transaction.setTransactionStatus(FAILED);
        } else {
            senderAccount.setBalance(senderAccount.getBalance().subtract(transaction.getAmount()));
            recepientAccount.setBalance(recepientAccount.getBalance().add(transaction.getAmount()));
            transaction.setTransactionStatus(COMPLETED);
        }
        emailService.sendTransactionMail(transaction, senderAccount);
        emailService.sendTransactionMail(transaction, recepientAccount);
    }


    @Override
    public Page<TransactionDetailedResp> transactionHistory(Long customerId, Map<String, String> allParams, Pageable pageable) {
        AtomicReference<List<Transaction>> transactions = new AtomicReference<>(transactionRepository.findByCustomerId(customerId, Pageable.unpaged(pageable.getSort())).toList());
        allParams.forEach((key, value) -> transactions.set(switch (key) {
            case "amount":
                yield transactions.get().stream()
                        .filter(transaction -> transaction.getAmount().setScale(2, RoundingMode.HALF_UP)
                                .compareTo(new BigDecimal(value).setScale(2, RoundingMode.HALF_UP)) == 0)
                        .toList();
            case "currencyId":
                yield transactions.get().stream()
                        .filter(transaction -> Objects.equals(transaction.getCurrencyId(), Long.valueOf(value)))
                        .toList();
            case "timestamp":
                yield transactions.get().stream()
                        .filter(transaction -> transaction.getTimestamp().truncatedTo(DAYS)
                                .isEqual(LocalDateTime.parse(value).truncatedTo(DAYS)))
                        .toList();
            case "message":
                yield transactions.get().stream()
                        .filter(transaction -> transaction.getMessage().contains(value))
                        .toList();
            case "transactionStatus":
                yield transactions.get().stream()
                        .filter(transaction -> transaction.getTransactionStatus().equals(TransactionStatus.valueOf(value)))
                        .toList();
            case "accountId":
                yield transactions.get().stream()
                        .filter(transaction -> Objects.equals(transaction.getSenderAccountId(), Long.valueOf(value)) ||
                                Objects.equals(transaction.getRecipientAccountId(), Long.valueOf(value)))
                        .toList();
            default:
                yield transactions.get();
        }));

        final int start = (int) pageable.getOffset();
        final int end = Math.min((start + pageable.getPageSize()), transactions.get().size());

        final List<TransactionDetailedResp> pageContent = transactions.get().subList(start, end).stream()
                .map(e -> transactionMapper.toTransactionDetailedResp(
                        e,
                        accountService.findById(e.getSenderAccountId()),
                        accountService.findById(e.getRecipientAccountId())))
                .toList();
        return new PageImpl<>(pageContent, pageable, transactions.get().size());
    }
}
