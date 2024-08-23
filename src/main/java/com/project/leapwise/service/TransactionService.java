package com.project.leapwise.service;

import com.project.leapwise.dto.TransactionDetailedResp;
import com.project.leapwise.dto.TransactionReq;
import com.project.leapwise.dto.TransactionResp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface TransactionService {
    TransactionResp createTransaction(TransactionReq transactionReq);

    Page<TransactionDetailedResp> transactionHistory(Long customerId, Map<String, String> allParams, Pageable pageable);
}
