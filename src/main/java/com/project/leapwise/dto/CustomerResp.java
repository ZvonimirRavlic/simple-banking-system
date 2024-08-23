package com.project.leapwise.dto;

import java.math.BigDecimal;
import java.util.List;

public record CustomerResp(
        Long customerId,
        String name,
        String address,
        String email,
        String phoneNumber,
        BigDecimal totalBalance,
        List<AccountResp> accounts) {
}
