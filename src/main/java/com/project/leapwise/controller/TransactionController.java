package com.project.leapwise.controller;

import com.project.leapwise.dto.TransactionDetailedResp;
import com.project.leapwise.dto.TransactionReq;
import com.project.leapwise.dto.TransactionResp;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping(value = "/transaction", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public interface TransactionController {


    @PostMapping
    TransactionResp createTransaction(@RequestBody @Valid TransactionReq transactionReq);

    @GetMapping(value = "/history/{customerId}")
    Page<TransactionDetailedResp> transactionHistory(@PathVariable("customerId") Long customerId,
                                                     @RequestParam Map<String, String> allParams,
                                                     Pageable pageable);
}
