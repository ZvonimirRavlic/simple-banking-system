package com.project.leapwise.dto;

import java.math.BigDecimal;

public record AccountResp(
        Long accountId,
        String accountNumber,
        String accountType,
        BigDecimal balance,
        BigDecimal pastMonthTurnover) {
}
