package com.project.leapwise.controller.impl;

import com.project.leapwise.controller.TransactionController;
import com.project.leapwise.dto.TransactionDetailedResp;
import com.project.leapwise.dto.TransactionReq;
import com.project.leapwise.dto.TransactionResp;
import com.project.leapwise.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TransactionControllerImpl implements TransactionController {

    private final TransactionService transactionService;


    @Override
    public TransactionResp createTransaction(TransactionReq transactionReq) {
        return transactionService.createTransaction(transactionReq);
    }

    @Override
    public Page<TransactionDetailedResp> transactionHistory(Long customerId, Map<String, String> allParams, Pageable pageable) {
        return transactionService.transactionHistory(customerId, allParams, pageable);
    }
}
