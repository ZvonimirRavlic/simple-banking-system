package com.project.leapwise.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionDetailedResp(
        Long transactionId,
        AccountTransactionResp senderAccount,
        AccountTransactionResp recipientAccount,
        BigDecimal amount,
        Long currencyId,
        String message,
        LocalDateTime timestamp,
        String transactionStatus
) {
}
