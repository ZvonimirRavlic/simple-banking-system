package com.project.leapwise.dto;

public record AccountTransactionResp(
        Long accountId,
        String accountNumber,
        String accountType
) {
}
