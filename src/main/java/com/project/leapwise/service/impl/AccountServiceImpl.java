package com.project.leapwise.service.impl;

import com.project.leapwise.db.entity.Account;
import com.project.leapwise.db.repository.AccountRepository;
import com.project.leapwise.service.AccountService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    @Transactional
    @Scheduled(cron = "${service-properties.account-turnover-update-cron}")
    public void calculateAndUpdateTurnover() {
        log.info("Starting account turnover update.");
        accountRepository.updatePastMonthTurnover();
        log.info("Account turnover update finished.");
    }

    @Override
    public Account findById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Account with accountId: %d doesn't exist!", accountId)));
    }
}
