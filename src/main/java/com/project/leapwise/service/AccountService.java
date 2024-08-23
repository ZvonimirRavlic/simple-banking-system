package com.project.leapwise.service;

import com.project.leapwise.db.entity.Account;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.validation.annotation.Validated;

@Validated
public interface AccountService {
    Account findById(@NotNull Long accountId);

    @Transactional
    @Scheduled(cron = "${service-properties.account-turnover-update-cron}")
    void calculateAndUpdateTurnover();
}
