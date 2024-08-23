package com.project.leapwise.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransactionReq(

        @NotNull(message = "Sender account ID cannot be null")
        Long senderAccountId,

        @NotNull(message = "Recipient account ID cannot be null")
        Long recipientAccountId,

        @NotNull(message = "Amount cannot be null")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,

        @NotNull(message = "Currency ID cannot be null")
        Long currencyId,

        @Size(max = 255, message = "Message cannot exceed 255 characters")
        String message
) {
}
